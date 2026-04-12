package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public boolean exists(String name) {
        return genreRepository.findByName(name).isPresent();
    }
}
