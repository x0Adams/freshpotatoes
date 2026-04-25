package hu.notkulonme.DataTransferer.entity.dto;

import hu.notkulonme.DataTransferer.entity.Continent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "continent")
public record ContinentMongo(
        @Id
        String qid,
        String name
) implements DumpDocument {
    public Continent toEntity() {
        Continent continent = new Continent();
        continent.setId(getIdFromQid());
        continent.setName(safeName(name));
        return continent;
    }

    public static Continent defaultEntity() {
        Continent continent = new Continent();
        continent.setId(0);
        continent.setName("None");
        return continent;
    }
}
