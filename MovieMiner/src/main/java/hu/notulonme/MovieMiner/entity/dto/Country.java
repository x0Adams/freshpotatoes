package hu.notulonme.MovieMiner.entity.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "country")
public record Country(
        @Id
        String qid,
        String name,
        String continentQid
) implements DumpDocument {
}
