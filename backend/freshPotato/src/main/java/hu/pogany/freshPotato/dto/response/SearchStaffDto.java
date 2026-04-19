package hu.pogany.freshPotato.dto.response;

import java.time.LocalDate;

public record SearchStaffDto(
        Integer id,
        String name,
        GenderDto gender,
        LocalDate birthDay,
        CountryDto birthCountry
) {
}

