package hu.notkulonme.DataTransferer.entity.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "movie")
public record FilmMongo(
        @Id
        String qid,
        String title,
        int duration,
        String releaseDate,
        List<String> genres,
        List<String> actors,
        List<String> directors,
        List<String> productionCountries,
        String youtubeId,
        String wikipediaTitle
) implements DumpDocument{
}
