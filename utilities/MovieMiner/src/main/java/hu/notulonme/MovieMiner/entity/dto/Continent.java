package hu.notulonme.MovieMiner.entity.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "continent")
public record Continent(
        @Id
        String qid,
        String name
) implements DumpDocument{
}
