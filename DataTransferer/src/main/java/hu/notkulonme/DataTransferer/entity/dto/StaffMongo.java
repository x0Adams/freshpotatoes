package hu.notkulonme.DataTransferer.entity.dto;

import hu.notkulonme.DataTransferer.entity.Country;
import hu.notkulonme.DataTransferer.entity.Gender;
import hu.notkulonme.DataTransferer.entity.Staff;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.DateTimeException;
import java.util.List;
import java.util.Map;

@Document(collection = "staff")
public record StaffMongo(
        @Id
        String qid,
        String name,
        String genderQid,
        List<String> citizenships,
        String birthday
) implements DumpDocument{
    public Staff toEntity(Map<Integer, Gender> genders, Map<Integer, Country> countries) {
        Staff staff = new Staff();
        staff.setId(getIdFromQid());
        staff.setName(safeName(name));
        staff.setBirthday(parseDateOrDefault(birthday));

        int genderId = parseQid(genderQid);
        Gender gender = genders.getOrDefault(genderId, genders.get(0));
        if (gender == null) {
            gender = GenderMongo.defaultEntity();
        }
        staff.setGender(gender);

        int countryId = 0;
        if (!safeList(citizenships).isEmpty()) {
            countryId = parseQid(safeList(citizenships).getFirst());
        }
        Country country = countries.getOrDefault(countryId, countries.get(0));
        if (country == null) {
            country = CountryMongo.defaultEntity(null);
        }
        staff.setBirthCountry(country);
        return staff;
    }

    private LocalDate parseDateOrDefault(String date) {
        List<String> parts = splitDate(date);
        int year = toIntOrDefault(parts.size() > 0 ? parts.get(0) : null, 1900);
        int month = toIntOrDefault(parts.size() > 1 ? parts.get(1) : null, 1);
        int day = toIntOrDefault(parts.size() > 2 ? parts.get(2) : null, 1);
        month = month <= 0 ? 1 : month;
        day = day <= 0 ? 1 : day;
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            return LocalDate.of(1900, 1, 1);
        }
    }
}
