package hu.notulonme.MovieMiner.entity.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "staff")
public record Staff(
        @Id
        String qid,
        String name,
        String genderQid,
        List<String> citizenships,
        Date birthday
) implements DumpDocument{
}
