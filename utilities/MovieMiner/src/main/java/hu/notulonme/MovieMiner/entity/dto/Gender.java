package hu.notulonme.MovieMiner.entity.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gender")
public record Gender(
        @Id
        String qid,
        String name
) implements DumpDocument{
}
