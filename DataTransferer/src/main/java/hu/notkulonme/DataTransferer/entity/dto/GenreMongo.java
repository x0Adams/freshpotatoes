package hu.notkulonme.DataTransferer.entity.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "genre")
public record GenreMongo(
        @Id
        String qid,
        String name
) implements DumpDocument{
}
