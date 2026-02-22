package hu.notulonme.MovieMiner.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import hu.notulonme.MovieMiner.entity.dto.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class DumpEntityProccessor implements Runnable {
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
        this.wikiDump = JsonPath.parse(wikiDumpStr);
        if (wikiDump == null || wikiPath == null)
            return;

        String instanceOf = read("$.extraction_paths.common.instance_of_ids", String.class);
        if (instanceOf == null) return;

        switch (instanceOf) {
            case "Q11424", "Q24862" -> proccessMovieData();
            case "Q5107" -> proccessContinentData();
            case "Q6256" -> proccessCountryData();
            case "Q201658" -> proccessFilmGenreData();
            case "Q5" -> proccessHumanData();
            case "Q48277" -> proccessGenderData();
        }
    }

    private void proccessMovieData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String title = read("$.extraction_paths.common.english_label", String.class);
        Integer duration = 0;
        String durationStr = read("$.extraction_paths.specific_schemas.film.duration_minutes_qty", String.class);
        if (durationStr != null) {
            try {
                // The value can be like "+90", which needs to be cleaned for parsing.
                duration = Integer.parseInt(durationStr.replace("+", ""));
            } catch (NumberFormatException e) {
                // Ignore if parsing fails, duration remains 0
            }
        }
        Date releaseDate = getDate("$.extraction_paths.specific_schemas.film.release_date_time");
        List<String> genres = readList("$.extraction_paths.specific_schemas.film.genre_ids");
        List<String> actors = readList("$.extraction_paths.specific_schemas.film.actor_ids");
        List<String> directors = readList("$.extraction_paths.specific_schemas.film.director_ids");
        List<String> productionCountries = readList("$.extraction_paths.specific_schemas.film.production_country_ids");
        String youtubeId = read("$.extraction_paths.specific_schemas.film.youtube_video_id", String.class);
        String googleKnowledGraphId = read("$.extraction_paths.specific_schemas.film.google_knowledge_graph_id", String.class);
        String wikipediaTitle = read("$.extraction_paths.specific_schemas.film.wikipedia_title_api", String.class);

        batcher.save(new Film(qid, title, duration, releaseDate, genres, actors, directors, productionCountries, youtubeId, googleKnowledGraphId, wikipediaTitle));
    }

    private void proccessContinentData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String name = read("$.extraction_paths.common.english_label", String.class);
        batcher.save(new Continent(qid, name));
    }

    private void proccessCountryData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String name = read("$.extraction_paths.common.english_label", String.class);
        String continentQid = read("$.extraction_paths.specific_schemas.country.continent_id", String.class);
        batcher.save(new Country(qid, name, continentQid));
    }

    private void proccessFilmGenreData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String name = read("$.extraction_paths.common.english_label", String.class);
        batcher.save(new Genre(qid, name));
    }

    private void proccessHumanData() {
        String qid = read("$.extraction_paths.common.entity_id", String.class);
        String name = read("$.extraction_paths.common.english_label", String.class);
        String genderQid = read("$.extraction_paths.specific_schemas.actor_or_director.gender_id", String.class);
        List<String> citizenships = readList("$.extraction_paths.specific_schemas.actor_or_director.citizenship_country_id");
        Date birthday = getDate("$.extraction_paths.specific_schemas.actor_or_director.birthday_time");
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

    private Date getDate(String wikiPathExpr) {
        String value = read(wikiPathExpr, String.class);
        if (value == null) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("'+'yyyy-MM-dd'T'HH:mm:ss'Z'");
            return sdf.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
}
