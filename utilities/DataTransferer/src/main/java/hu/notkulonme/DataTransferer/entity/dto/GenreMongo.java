package hu.notkulonme.DataTransferer.entity.dto;

import hu.notkulonme.DataTransferer.entity.Genre;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "genre")
public record GenreMongo(
        @Id
        String qid,
        String name
) implements DumpDocument{
    public Genre toEntity() {
        Genre genre = new Genre();
        genre.setId(getIdFromQid());
        genre.setName(safeName(name));
        return genre;
    }

    public static Genre defaultEntity() {
        Genre genre = new Genre();
        genre.setId(0);
        genre.setName("None");
        return genre;
    }
}
