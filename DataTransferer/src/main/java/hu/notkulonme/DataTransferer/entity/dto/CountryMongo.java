package hu.notkulonme.DataTransferer.entity.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "country")
public record CountryMongo(
        @Id
        String qid,
        String name,
        List<String> continentQid //should be a list
) implements DumpDocument {
}
