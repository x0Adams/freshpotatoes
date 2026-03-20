package hu.pogany.freshPotato.dto;

import hu.pogany.freshPotato.entity.Gender;

import java.time.LocalDate;

public record MovieStaffDto(Integer id, String name, GenderDto gender, LocalDate birthDay, CountryDto birthCountry) {
}
