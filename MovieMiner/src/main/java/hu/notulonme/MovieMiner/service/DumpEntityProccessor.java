package hu.notulonme.MovieMiner.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import hu.notulonme.MovieMiner.entity.dto.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class DumpEntityProccessor implements Runnable {
    private static final Logger log = LogManager.getLogger(DumpEntityProccessor.class);
    private DocumentContext wikiPath;
    private String wikiDumpStr;
    private DocumentContext wikiDump;
    private JsonBatcherService batcher;


    public DumpEntityProccessor(DocumentContext wikiPath, JsonBatcherService batcher) {
        this.wikiPath = wikiPath;
        this.batcher = batcher;
    }

    public void setWikiDump(String wikiDump) {
        this.wikiDumpStr = wikiDump;
    }

    @Override
    public void run() {
        try {
            this.wikiDump = JsonPath.parse(wikiDumpStr);
        } catch (RuntimeException e) {
            log.debug("not valid json");
            return;
        }

        if (wikiDump == null || wikiPath == null)
            return;

        List<String> instanceOf = readList("$.extraction_paths.common.instance_of_ids");

        if (instanceOf.contains("Q11424") || instanceOf.contains("Q24862") || instanceOf.contains("Q202866") || instanceOf.contains("Q20650540")) {
            proccessMovieData();
        }
        if (instanceOf.contains("Q5107")) {
            proccessContinentData();
        }
        if (instanceOf.contains("Q6256")) {
            proccessCountryData();
        }
        if (instanceOf.contains("Q201658")) {
            proccessFilmGenreData();
        }
        if (instanceOf.contains("Q5")) {
            proccessHumanData();
        }
        if (instanceOf.contains("Q48277")) {
            proccessGenderData();
        }
    }

    private void proccessMovieData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String title = read("$.extraction_paths.common.english_label", String.class);
        int duration = 0;
        String durationStr = read("$.extraction_paths.specific_schemas.film.duration_minutes", String.class);
        if (durationStr != null) {
            try {
                // The value can be like "+90", which needs to be cleaned for parsing.
                duration = Integer.parseInt(durationStr.replace("+", ""));
            } catch (NumberFormatException e) {
                // Ignore if parsing fails, duration remains 0
            }
        }
        String releaseDate = getDate("$.extraction_paths.specific_schemas.film.release_date_time");
        List<String> genres = readList("$.extraction_paths.specific_schemas.film.genre_ids");
        List<String> actors = readList("$.extraction_paths.specific_schemas.film.actor_ids");
        List<String> directors = readList("$.extraction_paths.specific_schemas.film.director_ids");
        List<String> productionCountries = readList("$.extraction_paths.specific_schemas.film.production_country_ids");
        String youtubeId = read("$.extraction_paths.specific_schemas.film.youtube_video_id", String.class);
        String wikipediaTitle = read("$.extraction_paths.specific_schemas.film.wikipedia_title_api", String.class);

        batcher.save(new Film(qid, title, duration, releaseDate, genres, actors, directors, productionCountries, youtubeId, wikipediaTitle));
    }

    private void proccessContinentData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String name = read("$.extraction_paths.common.english_label", String.class);
        batcher.save(new Continent(qid, name));
    }

    private void proccessCountryData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String name = read("$.extraction_paths.common.english_label", String.class);
        List<String> continentQid = readList("$.extraction_paths.specific_schemas.country.continent_id");
        batcher.save(new Country(qid, name, continentQid));
    }

    private void proccessFilmGenreData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String name = read("$.extraction_paths.common.english_label", String.class);
        batcher.save(new Genre(qid, name));
    }

    private void proccessHumanData() {
        List<String> occupations = readList("$.extraction_paths.specific_schemas.actor_or_director.occupation_ids");

        if (occupations.stream().noneMatch(id -> id.equals("Q33999") || id.equals("Q2526255")))
            return;

        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String name = read("$.extraction_paths.common.english_label", String.class);
        String genderQid = read("$.extraction_paths.specific_schemas.actor_or_director.gender_id", String.class);
        List<String> citizenships = readList("$.extraction_paths.specific_schemas.actor_or_director.citizenship_country_id");
        String birthday = getDate("$.extraction_paths.specific_schemas.actor_or_director.birthday_time");
        batcher.save(new Staff(qid, name, genderQid, citizenships, birthday));
    }

    private void proccessGenderData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String name = read("$.extraction_paths.common.english_label", String.class);
        batcher.save(new Gender(qid, name));
    }

    private <T> T read(String wikiPathExpr, Class<T> type) {
        try {
            String actualPath = wikiPath.read(wikiPathExpr);
            return wikiDump.read(actualPath, type);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> readList(String wikiPathExpr) {
        try {
            String actualPath = wikiPath.read(wikiPathExpr);
            return wikiDump.read(actualPath, List.class);
        } catch (Exception e) {
            try {
                String actualPath = wikiPath.read(wikiPathExpr);
                String singleValue = wikiDump.read(actualPath, String.class);
                return (singleValue != null) ? Collections.singletonList(singleValue) : Collections.emptyList();
            } catch (Exception e2) {
                return Collections.emptyList();
            }
        }
    }

    private String getDate(String wikiPathExpr) {
        String value = read(wikiPathExpr, String.class);
        if (value == null) return null;
        try {
            if (value.startsWith("+")) {
                value = value.substring(1);
            }

            return value.substring(0, value.indexOf("T"));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
