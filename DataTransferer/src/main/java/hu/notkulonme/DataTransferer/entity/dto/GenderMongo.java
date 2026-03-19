package hu.notkulonme.DataTransferer.entity.dto;

import hu.notkulonme.DataTransferer.entity.Gender;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gender")
public record GenderMongo(
        @Id
        String qid,
        String name
) implements DumpDocument{
    public Gender toEntity() {
        Gender gender = new Gender();
        gender.setId(getIdFromQid());
        gender.setName(safeName(name));
        return gender;
    }

    public static Gender defaultEntity() {
        Gender gender = new Gender();
        gender.setId(0);
        gender.setName("None");
        return gender;
    }
}
