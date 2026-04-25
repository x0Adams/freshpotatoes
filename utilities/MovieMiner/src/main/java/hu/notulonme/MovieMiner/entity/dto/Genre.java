package hu.notulonme.MovieMiner.entity.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "genre")
public record Genre(
        @Id
        String qid,
        String name
) implements DumpDocument{
}
