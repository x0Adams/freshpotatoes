package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.response.GenreDto;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GenreService {
    private final Mapper mapper;
    private final GenreRepository genreRepository;

    public GenreService(Mapper mapper, GenreRepository genreRepository) {
        this.mapper = mapper;
        this.genreRepository = genreRepository;
    }

    public boolean exists(String name) {
        return genreRepository.findByName(name).isPresent();
    }

    public List<GenreDto> getAll() {
        return mapper.toGenreDtoList(genreRepository.findAll());
    }
}
