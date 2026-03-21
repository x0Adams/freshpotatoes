package hu.notkulonme.DataTransferer.entity.dto;

import hu.notkulonme.DataTransferer.entity.Continent;
import hu.notkulonme.DataTransferer.entity.Country;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Document(collection = "country")
public record CountryMongo(
        @Id
        String qid,
        String name,
        List<String> continentQid //should be a list
) implements DumpDocument {
    public Country toEntity(Map<Integer, Continent> continents) {
        Country country = new Country();
        country.setId(getIdFromQid());
        country.setName(safeName(name));
        country.setContinents(
                safeList(continentQid).stream()
                        .map(this::parseQid)
                        .filter(id -> id > 0)
                        .distinct()
                        .map(continents::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(HashSet::new))
        );
        return country;
    }

    public static Country defaultEntity(Set<Continent> continents) {
        Country country = new Country();
        country.setId(0);
        country.setName("None");
        country.setContinents(continents == null ? new HashSet<>() : new HashSet<>(continents));
        return country;
    }
}
