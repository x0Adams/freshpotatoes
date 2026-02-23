package hu.notulonme.MovieMiner.service;

import hu.notulonme.MovieMiner.entity.dto.*;
import hu.notulonme.MovieMiner.repository.mongo.*;
import hu.notulonme.MovieMiner.util.Batch;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonBatcherService {
    private Batch<Continent> continentBatch;
    private Batch<Country> countryBatch;
    private Batch<Film> filmBatch;
    private Batch<Gender> genderBatch;
    private Batch<Genre> genreBatch;
    private Batch<Staff> staffBatch;

    @Autowired
    private ContinentRepository continentRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private FilmRepository filmRepository;
    @Autowired
    private GenderRepository genderRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private GenreRepository genreRepository;

    public JsonBatcherService() {
        continentBatch = new Batch<>();
        countryBatch = new Batch<>();
        filmBatch = new Batch<>();
        genderBatch = new Batch<>();
        genreBatch = new Batch<>();
        staffBatch = new Batch<>();
    }


    public void save(Continent continent) {
        continentBatch.put(continent);
        if (continentBatch.size() >= Batch.BATCH_SIZE){
            continentRepository.saveAll(continentBatch.drain());
        }
    }

    public void save(Country country) {
        countryBatch.put(country);
        if (countryBatch.size() >= Batch.BATCH_SIZE){
            countryRepository.saveAll(countryBatch.drain());
        }
    }

    public void save(Film film) {
        filmBatch.put(film);
        if (filmBatch.size() >= Batch.BATCH_SIZE){
            filmRepository.saveAll(filmBatch.drain());
        }
    }

    public void save(Gender gender) {
        genderBatch.put(gender);
        if (genderBatch.size() >= Batch.BATCH_SIZE){
            genderRepository.saveAll(genderBatch.drain());
        }
    }

    public void save(Genre genre) {
        genreBatch.put(genre);
        if (genreBatch.size() >= Batch.BATCH_SIZE){
            genreRepository.saveAll(genreBatch.drain());
        }
    }

    public void save(Staff staff) {
        staffBatch.put(staff);
        if (staffBatch.size() >= Batch.BATCH_SIZE){
            staffRepository.saveAll(staffBatch.drain());
        }
    }

    @PreDestroy
    public void saveAllBatch() {
        continentRepository.saveAll(continentBatch.drain());
        countryRepository.saveAll(countryBatch.drain());
        filmRepository.saveAll(filmBatch.drain());
        genderRepository.saveAll(genderBatch.drain());
        genreRepository.saveAll(genreBatch.drain());
        staffRepository.saveAll(staffBatch.drain());
    }
}
