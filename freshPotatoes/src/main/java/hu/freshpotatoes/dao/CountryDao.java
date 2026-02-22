package hu.freshpotatoes.dao;

import hu.freshpotatoes.model.Country;

public interface CountryDao extends Dao<Country, Integer> {
    String findByName(String countryName);
}
