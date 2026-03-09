package hu.notkulonme.DataTransferer.entity.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gender")
public record GenderMongo(
        @Id
        String qid,
        String name
) implements DumpDocument{
}
