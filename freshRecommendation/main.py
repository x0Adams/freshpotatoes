import asyncio
import inspect
import json
import logging
import os
import time
from contextlib import asynccontextmanager, suppress
from dataclasses import dataclass
from pathlib import Path
from threading import RLock
from typing import Any

import pandas as pd
from fastapi import FastAPI, HTTPException, Query
from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine
from sqlalchemy.engine.url import URL, make_url

try:
    import aio_pika
except ModuleNotFoundError:
    aio_pika = None

try:
    from libreco.algorithms import PinSage
    from libreco.data import DatasetFeat, DatasetPure
except ModuleNotFoundError:
    PinSage = None
    DatasetFeat = None
    DatasetPure = None


LOGGER = logging.getLogger("pinsage-recommender")
logging.basicConfig(level=logging.INFO)
LOGGER.setLevel(logging.INFO)


@dataclass
class Settings:
    db_url: str
    db_pass: str
    rabbitmq_url: str
    rabbitmq_queue: str
    rabbitmq_purge_on_startup: bool
    retrain_min_events: int
    retrain_max_delay_sec: float
    retrain_poll_interval_sec: float
    schema_path: Path

    @classmethod
    def from_env(cls) -> "Settings":
        base_path = Path(__file__).resolve().parent
        purge_on_startup = os.getenv("RABBITMQ_PURGE_ON_STARTUP", "false").strip().lower() in {
            "1",
            "true",
            "yes",
            "y",
            "on",
        }

        def env_int(name: str, default: int) -> int:
            raw = os.getenv(name, str(default)).strip()
            try:
                return max(1, int(raw))
            except ValueError:
                LOGGER.warning("Invalid %s=%r, using default=%s", name, raw, default)
                return default

        def env_float(name: str, default: float) -> float:
            raw = os.getenv(name, str(default)).strip()
            try:
                return max(0.1, float(raw))
            except ValueError:
                LOGGER.warning("Invalid %s=%r, using default=%s", name, raw, default)
                return default

        return cls(
            db_url=os.getenv("DB_URL", ""),
            db_pass=os.getenv("DB_PASS", ""),
            rabbitmq_url=os.getenv("RABBITMQ_URL", "amqp://guest:guest@localhost/"),
            rabbitmq_queue=os.getenv("RABBITMQ_QUEUE", "movie_ratings"),
            rabbitmq_purge_on_startup=purge_on_startup,
            retrain_min_events=env_int("RETRAIN_MIN_EVENTS", 200),
            retrain_max_delay_sec=env_float("RETRAIN_MAX_DELAY_SEC", 60.0),
            retrain_poll_interval_sec=env_float("RETRAIN_POLL_INTERVAL_SEC", 2.0),
            schema_path=base_path / "fresh_potatoes_structure.sql",
        )


class RecommenderService:
    def __init__(self, settings: Settings) -> None:
        self.settings = settings
        self.engine = self._build_engine(settings.db_url, settings.db_pass)
        self.lock = RLock()
        self.model: Any | None = None
        self.data_info: Any | None = None
        self.trainable_user_ids: set[int] = set()
        self.latest_movies_df = pd.DataFrame()
        self.latest_ratings_df = pd.DataFrame()
        self.latest_users_df = pd.DataFrame()
        self.consumer_task: asyncio.Task | None = None
        self.retrainer_task: asyncio.Task | None = None
        self.pending_retrain_events = 0
        self.last_retrain_finished_monotonic = 0.0
        self.retrain_signal: asyncio.Event | None = None

    @staticmethod
    def _build_engine(db_url: str, db_pass: str) -> Engine:
        if not db_url:
            raise RuntimeError("DB_URL is required")
        url: URL = make_url(db_url)
        if "+" not in url.drivername and url.drivername == "mysql":
            url = url.set(drivername="mysql+pymysql")
        if db_pass:
            url = url.set(password=db_pass)
        return create_engine(url, pool_pre_ping=True)

    def ensure_schema(self) -> None:
        with self.engine.begin() as conn:
            rows = conn.execute(text("SHOW TABLES"))
            existing_tables = {row[0] for row in rows}
            required_tables = {
                "movie",
                "rate",
                "users",
                "staff",
                "staff_role_in_movie",
                "productions_country",
                "country",
                "country_continent",
                "continent",
                "genre",
                "genre_movie",
                "gender",
            }
            if required_tables.issubset(existing_tables):
                return
            if existing_tables:
                missing = sorted(required_tables - existing_tables)
                raise RuntimeError(
                    "Database schema is partially initialized; missing tables: " + ", ".join(missing)
                )
        LOGGER.info("Database schema not found, applying SQL bootstrap script")
        statements = self._split_sql_script(self.settings.schema_path.read_text(encoding="utf-8"))
        with self.engine.begin() as conn:
            for statement in statements:
                conn.exec_driver_sql(statement)

    @staticmethod
    def _split_sql_script(script: str) -> list[str]:
        statements: list[str] = []
        buff: list[str] = []
        for line in script.splitlines():
            stripped = line.strip()
            if not stripped or stripped.startswith("--"):
                continue
            if stripped.startswith("/*!"):
                continue
            upper = stripped.upper()
            if upper.startswith("SET SQL_MODE") or upper.startswith("SET TIME_ZONE"):
                continue
            if upper.startswith("START TRANSACTION") or upper == "COMMIT;":
                continue
            buff.append(line)
            if stripped.endswith(";"):
                full = "\n".join(buff).strip()
                if full:
                    statements.append(full)
                buff = []
        if buff:
            full = "\n".join(buff).strip()
            if full:
                statements.append(full)
        return statements

    def _load_ratings_df(self) -> pd.DataFrame:
        query = """
            SELECT user_id, movie_id, rating, time
            FROM rate
            WHERE rating BETWEEN 1 AND 5
        """
        ratings_df = pd.read_sql(query, self.engine)
        ratings_df["user_id"] = ratings_df["user_id"].astype(int)
        ratings_df["movie_id"] = ratings_df["movie_id"].astype(int)
        ratings_df["rating"] = ratings_df["rating"].astype(float)
        return ratings_df

    def _load_movie_df(self) -> pd.DataFrame:
        movies = pd.read_sql(
            """
            SELECT id AS movie_id, name, duration, release_date
            FROM movie
            """,
            self.engine,
        )
        directors = pd.read_sql(
            """
            SELECT srm.movie_id, s.name AS person_name, g.name AS person_gender
            FROM staff_role_in_movie srm
            JOIN staff s ON s.id = srm.staff_id
            JOIN gender g ON g.id = s.gender_id
            WHERE srm.role = 'DIRECTOR'
            ORDER BY srm.movie_id, srm.staff_id
            """,
            self.engine,
        )
        actors = pd.read_sql(
            """
            SELECT srm.movie_id, s.name AS person_name, g.name AS person_gender
            FROM staff_role_in_movie srm
            JOIN staff s ON s.id = srm.staff_id
            JOIN gender g ON g.id = s.gender_id
            WHERE srm.role = 'ACTOR'
            ORDER BY srm.movie_id, srm.staff_id
            """,
            self.engine,
        )
        countries = pd.read_sql(
            """
            SELECT pc.movie_id, c.name AS country_name, cont.name AS continent_name
            FROM productions_country pc
            JOIN country c ON c.id = pc.country_id
            JOIN country_continent cc ON cc.country_id = c.id
            JOIN continent cont ON cont.id = cc.continent_id
            ORDER BY pc.movie_id, c.id
            """,
            self.engine,
        )
        genres = pd.read_sql(
            """
            SELECT gm.movie_id, g.name AS genre_name
            FROM genre_movie gm
            JOIN genre g ON g.id = gm.genre_id
            ORDER BY gm.movie_id, g.id
            """,
            self.engine,
        )

        movie_features = movies.copy()
        movie_features = movie_features.merge(
            self._pivot_limited(directors, value_col="person_name", prefix="director", limit=2),
            on="movie_id",
            how="left",
        )
        movie_features = movie_features.merge(
            self._pivot_limited(directors, value_col="person_gender", prefix="director_gender", limit=2),
            on="movie_id",
            how="left",
        )
        movie_features = movie_features.merge(
            self._pivot_limited(actors, value_col="person_name", prefix="actor", limit=5),
            on="movie_id",
            how="left",
        )
        movie_features = movie_features.merge(
            self._pivot_limited(actors, value_col="person_gender", prefix="actor_gender", limit=5),
            on="movie_id",
            how="left",
        )
        movie_features = movie_features.merge(
            self._pivot_limited(countries, value_col="country_name", prefix="country", limit=3),
            on="movie_id",
            how="left",
        )
        movie_features = movie_features.merge(
            self._pivot_limited(countries, value_col="continent_name", prefix="continent", limit=3),
            on="movie_id",
            how="left",
        )
        movie_features = movie_features.merge(
            self._pivot_limited(genres, value_col="genre_name", prefix="genre", limit=5),
            on="movie_id",
            how="left",
        )
        movie_features = movie_features.fillna("unknown")
        return movie_features

    def _load_users_df(self) -> pd.DataFrame:
        users_df = pd.read_sql(
            """
            SELECT u.id AS user_id, u.age, g.name AS gender_name
            FROM users u
            JOIN gender g ON g.id = u.gender_id
            """,
            self.engine,
        )
        users_df["user_id"] = users_df["user_id"].astype(int)
        users_df["age"] = pd.to_numeric(users_df["age"], errors="coerce").fillna(0).astype(int)
        users_df["gender_name"] = users_df["gender_name"].fillna("unknown")
        return users_df

    @staticmethod
    def _pivot_limited(df: pd.DataFrame, value_col: str, prefix: str, limit: int) -> pd.DataFrame:
        if df.empty:
            return pd.DataFrame(columns=["movie_id"] + [f"{prefix}_{i}" for i in range(1, limit + 1)])
        reduced = df.dropna(subset=["movie_id", value_col]).copy()
        reduced["slot"] = reduced.groupby("movie_id").cumcount() + 1
        reduced = reduced[reduced["slot"] <= limit]
        pivoted = reduced.pivot(index="movie_id", columns="slot", values=value_col).reset_index()
        pivoted = pivoted.rename(columns={i: f"{prefix}_{i}" for i in range(1, limit + 1)})
        for i in range(1, limit + 1):
            column = f"{prefix}_{i}"
            if column not in pivoted.columns:
                pivoted[column] = "unknown"
        return pivoted

    @staticmethod
    def _construct_pinsage(data_info: Any) -> Any:
        if PinSage is None:
            raise RuntimeError("librecommender is not installed")
        desired_args = {
            "task": "ranking",
            "data_info": data_info,
            "embed_size": 64,
            "n_epochs": 10,
            "lr": 0.001,
            "num_neg": 1,
            "seed": 42,
        }
        signature = inspect.signature(PinSage)
        valid_args = {k: v for k, v in desired_args.items() if k in signature.parameters}
        return PinSage(**valid_args)

    @staticmethod
    def _fit_model(model: Any, train_data: Any) -> None:
        fit_signature = inspect.signature(model.fit)
        fit_options = []
        if "neg_sampling" in fit_signature.parameters:
            fit_options.extend(
                [
                    {"neg_sampling": True, "verbose": 1},
                    {"neg_sampling": True},
                ]
            )
        fit_options.extend([{"verbose": 1}, {}])
        for kwargs in fit_options:
            try:
                model.fit(train_data, **kwargs)
                return
            except TypeError:
                continue
        model.fit(train_data)

    @staticmethod
    def _prepare_feature_train_df(
        ratings_df: pd.DataFrame, users_df: pd.DataFrame, movies_df: pd.DataFrame
    ) -> tuple[pd.DataFrame, list[str], list[str], list[str], list[str]]:
        train_df = ratings_df[["user_id", "movie_id", "rating"]].rename(
            columns={"user_id": "user", "movie_id": "item"}
        )
        # Binary feedback: ratings 4-5 are positive, 1-3 are negative.
        train_df["label"] = (pd.to_numeric(train_df["rating"], errors="coerce").fillna(0) >= 4).astype(int)
        train_df = train_df.drop(columns=["rating"])
        user_features = users_df.rename(columns={"user_id": "user"}).copy()
        item_features = movies_df.rename(columns={"movie_id": "item"}).copy()

        if "release_date" in item_features.columns:
            item_features["release_year"] = pd.to_datetime(
                item_features["release_date"], errors="coerce"
            ).dt.year
            item_features = item_features.drop(columns=["release_date"])

        feature_train_df = train_df.merge(user_features, on="user", how="left")
        feature_train_df = feature_train_df.merge(item_features, on="item", how="left")

        dense_cols = [col for col in ["age", "duration", "release_year"] if col in feature_train_df.columns]
        excluded_cols = {"user", "item", "label", *dense_cols}
        sparse_cols = [col for col in feature_train_df.columns if col not in excluded_cols]

        for col in dense_cols:
            feature_train_df[col] = pd.to_numeric(feature_train_df[col], errors="coerce").fillna(0.0)
        for col in sparse_cols:
            feature_train_df[col] = feature_train_df[col].fillna("unknown").astype(str)

        # DatasetFeat expects feature columns only; exclude identifier columns.
        user_cols = [col for col in user_features.columns if col in feature_train_df.columns and col != "user"]
        item_cols = [col for col in item_features.columns if col in feature_train_df.columns and col != "item"]
        return feature_train_df, sparse_cols, dense_cols, user_cols, item_cols

    def _build_trainset(
        self, ratings_df: pd.DataFrame, users_df: pd.DataFrame, movies_df: pd.DataFrame
    ) -> tuple[Any, Any]:
        base_df = ratings_df[["user_id", "movie_id", "rating"]].rename(
            columns={"user_id": "user", "movie_id": "item"}
        )
        # Binary feedback: ratings 4-5 are positive, 1-3 are negative.
        base_df["label"] = (pd.to_numeric(base_df["rating"], errors="coerce").fillna(0) >= 4).astype(int)
        base_df = base_df.drop(columns=["rating"])

        if DatasetFeat is not None:
            try:
                feat_df, sparse_cols, dense_cols, user_cols, item_cols = self._prepare_feature_train_df(
                    ratings_df, users_df, movies_df
                )
                LOGGER.info("DatasetFeat sparse feature columns: %s", sparse_cols)
                LOGGER.info("DatasetFeat dense feature columns: %s", dense_cols)
                LOGGER.info("DatasetFeat user feature columns: %s", user_cols)
                LOGGER.info("DatasetFeat item feature columns: %s", item_cols)
                build_fn = DatasetFeat.build_trainset
                signature = inspect.signature(build_fn)
                parameters = signature.parameters
                common_kwargs: dict[str, Any] = {}
                if "sparse_col" in parameters:
                    common_kwargs["sparse_col"] = sparse_cols
                if "dense_col" in parameters:
                    common_kwargs["dense_col"] = dense_cols
                if "user_col" in parameters:
                    common_kwargs["user_col"] = user_cols
                if "item_col" in parameters:
                    common_kwargs["item_col"] = item_cols

                if "train_data" in parameters:
                    train_data, data_info = build_fn(train_data=feat_df, **common_kwargs)
                else:
                    train_data, data_info = build_fn(feat_df, **common_kwargs)

                LOGGER.info(
                    "Using feature-aware trainset with %s sparse and %s dense columns",
                    len(sparse_cols),
                    len(dense_cols),
                )
                return train_data, data_info
            except Exception as exc:  # noqa: BLE001
                LOGGER.warning("Falling back to DatasetPure due to DatasetFeat error: %s", exc)

        if DatasetPure is None:
            raise RuntimeError("librecommender is not installed")
        LOGGER.info("Using DatasetPure trainset (feature-aware dataset unavailable)")
        return DatasetPure.build_trainset(base_df)

    def _log_queryable_user_ids(self) -> None:
        user_ids: list[int] = []
        if not self.latest_users_df.empty and "user_id" in self.latest_users_df.columns:
            user_ids = sorted(self.latest_users_df["user_id"].astype(int).unique().tolist())
        elif not self.latest_ratings_df.empty and "user_id" in self.latest_ratings_df.columns:
            user_ids = sorted(self.latest_ratings_df["user_id"].astype(int).unique().tolist())

        LOGGER.info("Queryable user ids (%s): %s", len(user_ids), user_ids)
        LOGGER.info("Model-trainable user ids from ratings (%s): %s", len(self.trainable_user_ids), sorted(self.trainable_user_ids))

    def train_model(self) -> None:
        if DatasetPure is None and DatasetFeat is None:
            raise RuntimeError("librecommender is not installed")
        with self.lock:
            ratings_df = self._load_ratings_df()
            movies_df = self._load_movie_df()
            users_df = self._load_users_df()
            self.latest_ratings_df = ratings_df
            self.latest_movies_df = movies_df
            self.latest_users_df = users_df

            if ratings_df.empty:
                self.model = None
                self.data_info = None
                self.trainable_user_ids = set()
                LOGGER.warning("No ratings available; model training skipped")
                self._log_queryable_user_ids()
                return

            positive_count = int((ratings_df["rating"] >= 4).sum())
            negative_count = int((ratings_df["rating"] < 4).sum())
            LOGGER.info(
                "Binary label distribution total=%s positive(4-5)=%s negative(1-3)=%s",
                len(ratings_df),
                positive_count,
                negative_count,
            )

            self.trainable_user_ids = set(ratings_df["user_id"].astype(int).tolist())

            train_data, data_info = self._build_trainset(ratings_df, users_df, movies_df)
            model = self._construct_pinsage(data_info)
            self._fit_model(model, train_data)
            self.model = model
            self.data_info = data_info
            LOGGER.info("PinSage training complete with %s ratings", len(ratings_df))
            self._log_queryable_user_ids()

    def note_new_rating_for_retrain(self) -> None:
        self.pending_retrain_events += 1
        if self.retrain_signal is not None:
            self.retrain_signal.set()

    async def run_background_retrainer(self) -> None:
        if self.retrain_signal is None:
            self.retrain_signal = asyncio.Event()
        if self.last_retrain_finished_monotonic <= 0:
            self.last_retrain_finished_monotonic = time.monotonic()

        while True:
            try:
                await asyncio.wait_for(self.retrain_signal.wait(), timeout=self.settings.retrain_poll_interval_sec)
            except asyncio.TimeoutError:
                pass
            self.retrain_signal.clear()

            pending = self.pending_retrain_events
            if pending <= 0:
                continue

            elapsed = time.monotonic() - self.last_retrain_finished_monotonic
            should_train = (
                pending >= self.settings.retrain_min_events
                or elapsed >= self.settings.retrain_max_delay_sec
            )
            if not should_train:
                continue

            self.pending_retrain_events = 0
            try:
                LOGGER.info(
                    "Background retrain triggered pending_events=%s min_events=%s elapsed_sec=%.2f",
                    pending,
                    self.settings.retrain_min_events,
                    elapsed,
                )
                await asyncio.to_thread(self.train_model)
                self.last_retrain_finished_monotonic = time.monotonic()
            except asyncio.CancelledError:
                self.pending_retrain_events += pending
                raise
            except Exception:  # noqa: BLE001
                self.pending_retrain_events += pending
                LOGGER.exception("Background retrain failed; pending events restored")
                await asyncio.sleep(1)

    @staticmethod
    def _extract_movie_ids(raw: Any, limit: int) -> list[int]:
        def append_if_int(value: Any, target: list[int]) -> None:
            try:
                target.append(int(value))
            except (TypeError, ValueError):
                return

        if raw is None:
            return []

        # Some librecommender versions return a DataFrame for recommend_user.
        if isinstance(raw, pd.DataFrame):
            movie_ids: list[int] = []
            candidate_cols = [col for col in ("item", "movie_id", "id") if col in raw.columns]
            if candidate_cols:
                for value in raw[candidate_cols[0]].tolist():
                    append_if_int(value, movie_ids)
                    if len(movie_ids) >= limit:
                        break
                return movie_ids
            # Fallback: use first column if known columns are missing.
            if len(raw.columns) > 0:
                for value in raw.iloc[:, 0].tolist():
                    append_if_int(value, movie_ids)
                    if len(movie_ids) >= limit:
                        break
            return movie_ids

        # Normalize array-like outputs (numpy arrays, etc.) to python lists.
        if hasattr(raw, "tolist") and not isinstance(raw, (list, tuple, dict, str, bytes)):
            raw = raw.tolist()

        if isinstance(raw, dict):
            # librecommender may return keyed outputs like {user_id: array([...])}.
            if "recommendation" in raw:
                raw = raw["recommendation"]
            elif "rec" in raw:
                raw = raw["rec"]
            elif len(raw) == 1:
                raw = next(iter(raw.values()))
            else:
                list_like_values = [v for v in raw.values() if isinstance(v, (list, tuple)) or hasattr(v, "tolist")]
                raw = list_like_values[0] if list_like_values else []

        if hasattr(raw, "tolist") and not isinstance(raw, (list, tuple, dict, str, bytes)):
            raw = raw.tolist()
        movie_ids: list[int] = []
        for entry in raw:
            if isinstance(entry, (list, tuple)) and entry:
                append_if_int(entry[0], movie_ids)
            elif isinstance(entry, dict):
                if "item" in entry:
                    append_if_int(entry["item"], movie_ids)
                elif "movie_id" in entry:
                    append_if_int(entry["movie_id"], movie_ids)
            else:
                append_if_int(entry, movie_ids)
            if len(movie_ids) >= limit:
                break
        return movie_ids

    def recommend(self, user_id: int, limit: int) -> list[int]:
        with self.lock:
            recommended: list[int] = []
            if self.model is not None:
                raw = None
                candidate_users: list[Any] = [user_id, str(user_id)]
                for candidate_user in candidate_users:
                    try:
                        raw = self.model.recommend_user(
                            user=candidate_user, n_rec=limit, inner_id=False, cold_start="average"
                        )
                    except TypeError:
                        try:
                            raw = self.model.recommend_user(user=candidate_user, n_rec=limit, inner_id=False)
                        except Exception as exc:  # noqa: BLE001
                            LOGGER.debug(
                                "recommend_user failed user=%s candidate=%r without cold_start: %s",
                                user_id,
                                candidate_user,
                                exc,
                            )
                            continue
                    except Exception as exc:  # noqa: BLE001
                        LOGGER.debug(
                            "recommend_user failed user=%s candidate=%r with cold_start: %s",
                            user_id,
                            candidate_user,
                            exc,
                        )
                        continue

                    recommended = self._extract_movie_ids(raw, limit)
                    if recommended:
                        break

                if not recommended:
                    LOGGER.info(
                        "Model returned no recommendations for user_id=%s raw_type=%s",
                        user_id,
                        type(raw).__name__ if raw is not None else "None",
                    )

            before_fallback = len(recommended)
            if len(recommended) < limit:
                fallback = self._fallback_recommend(user_id, limit)
                seen = set(recommended)
                for movie_id in fallback:
                    if movie_id not in seen:
                        recommended.append(movie_id)
                        seen.add(movie_id)
                    if len(recommended) >= limit:
                        break
            fallback_added = len(recommended) - before_fallback
            LOGGER.info(
                "Recommendation summary user_id=%s requested=%s model_count=%s fallback_added=%s final_count=%s",
                user_id,
                limit,
                before_fallback,
                fallback_added,
                len(recommended),
            )
            return recommended

    def _fallback_recommend(self, user_id: int, limit: int) -> list[int]:
        if self.latest_ratings_df.empty:
            return []
        ratings = self.latest_ratings_df
        seen_movies = set(
            ratings.loc[ratings["user_id"] == user_id, "movie_id"].astype(int).tolist()
        )
        popular = (
            ratings.groupby("movie_id", as_index=False)
            .agg(rate_count=("rating", "count"), rate_mean=("rating", "mean"))
            .sort_values(["rate_count", "rate_mean", "movie_id"], ascending=[False, False, True])
        )
        result: list[int] = []
        for movie_id in popular["movie_id"].tolist():
            movie_id_i = int(movie_id)
            if movie_id_i in seen_movies:
                continue
            result.append(movie_id_i)
            if len(result) >= limit:
                break
        return result

    def upsert_rating(self, user_id: int, movie_id: int, rating: int) -> None:
        with self.engine.begin() as conn:
            conn.execute(
                text(
                    """
                    INSERT INTO rate (user_id, movie_id, rating, time)
                    VALUES (:user_id, :movie_id, :rating, CURRENT_TIMESTAMP)
                    ON DUPLICATE KEY UPDATE rating = VALUES(rating), time = CURRENT_TIMESTAMP
                    """
                ),
                {"user_id": user_id, "movie_id": movie_id, "rating": rating},
            )

    @staticmethod
    def normalize_rating_event(payload: dict[str, Any]) -> tuple[int, int, int] | None:
        user_id = payload.get("userid", payload.get("user_id"))
        movie_id = payload.get("movieid", payload.get("movie_id"))
        rating = payload.get("rate", payload.get("rating"))
        try:
            user_id_i = int(user_id)
            movie_id_i = int(movie_id)
            rating_i = int(rating)
        except (TypeError, ValueError):
            return None
        if rating_i < 1 or rating_i > 5:
            return None
        return user_id_i, movie_id_i, rating_i

    async def consume_rabbitmq(self) -> None:
        if aio_pika is None:
            LOGGER.warning("aio-pika is not installed, RabbitMQ consumer disabled")
            return
        while True:
            connection = None
            try:
                LOGGER.info("Connecting RabbitMQ consumer to %s", self.settings.rabbitmq_url)
                connection = await aio_pika.connect_robust(self.settings.rabbitmq_url)
                channel = await connection.channel()
                queue = await channel.declare_queue(self.settings.rabbitmq_queue, durable=True)
                async with queue.iterator() as queue_iter:
                    async for message in queue_iter:
                        try:
                            async with message.process(ignore_processed=True, requeue=True):
                                try:
                                    payload = json.loads(message.body.decode("utf-8"))
                                except json.JSONDecodeError:
                                    LOGGER.warning("Skipping malformed RabbitMQ message: %s", message.body)
                                    continue
                                normalized = self.normalize_rating_event(payload)
                                if normalized is None:
                                    LOGGER.warning("Skipping invalid rating message: %s", payload)
                                    continue
                                user_id, movie_id, rating = normalized
                                await asyncio.to_thread(self.upsert_rating, user_id, movie_id, rating)
                                self.note_new_rating_for_retrain()
                                LOGGER.info(
                                    "Processed rating event user_id=%s movie_id=%s rating=%s pending_retrain_events=%s",
                                    user_id,
                                    movie_id,
                                    rating,
                                    self.pending_retrain_events,
                                )
                        except Exception:  # noqa: BLE001
                            LOGGER.exception("Failed processing RabbitMQ message; message will be requeued")
                            await asyncio.sleep(1)
            except asyncio.CancelledError:
                raise
            except Exception:  # noqa: BLE001
                LOGGER.exception("RabbitMQ consumer loop crashed, retrying in 5s")
                await asyncio.sleep(5)
            finally:
                if connection is not None:
                    await connection.close()

    async def purge_rabbitmq_queue_on_startup(self) -> None:
        if not self.settings.rabbitmq_purge_on_startup:
            return
        if aio_pika is None:
            LOGGER.warning("aio-pika is not installed, startup RabbitMQ purge skipped")
            return
        LOGGER.warning(
            "RABBITMQ_PURGE_ON_STARTUP is enabled; pending queue messages will be permanently discarded"
        )
        LOGGER.info("Purging pending RabbitMQ messages on queue=%s", self.settings.rabbitmq_queue)
        connection = await aio_pika.connect_robust(self.settings.rabbitmq_url)
        try:
            channel = await connection.channel()
            queue = await channel.declare_queue(self.settings.rabbitmq_queue, durable=True)
            purged_count = await queue.purge()
            LOGGER.info("Startup RabbitMQ purge complete, purged=%s", purged_count)
        finally:
            await connection.close()


settings = Settings.from_env()
service = RecommenderService(settings)


@asynccontextmanager
async def lifespan(_: FastAPI):
    service.ensure_schema()
    await asyncio.to_thread(service.train_model)
    service.last_retrain_finished_monotonic = time.monotonic()
    await service.purge_rabbitmq_queue_on_startup()
    service.retrain_signal = asyncio.Event()
    service.retrainer_task = asyncio.create_task(service.run_background_retrainer())
    service.consumer_task = asyncio.create_task(service.consume_rabbitmq())
    try:
        yield
    finally:
        if service.retrainer_task is not None:
            service.retrainer_task.cancel()
            with suppress(asyncio.CancelledError):
                await service.retrainer_task
        if service.consumer_task is not None:
            service.consumer_task.cancel()
            with suppress(asyncio.CancelledError):
                await service.consumer_task


app = FastAPI(title="Movie Recommendation API", lifespan=lifespan)


@app.get("/")
async def root() -> dict[str, str]:
    return {"status": "ok", "message": "PinSage recommender is running"}


@app.get("/recommend/{user_id}")
async def recommend(user_id: int, count: int = Query(default=10, ge=1, le=100)) -> dict[str, Any]:
    try:
        recommended = await asyncio.to_thread(service.recommend, user_id, count)
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    return {"user_id": user_id, "requested": count, "movie_ids": recommended}


@app.post("/retrain")
async def retrain() -> dict[str, str]:
    await asyncio.to_thread(service.train_model)
    service.last_retrain_finished_monotonic = time.monotonic()
    return {"status": "ok", "message": "Model retrained"}
