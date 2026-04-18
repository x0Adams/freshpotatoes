package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.dto.response.SearchStaffDto;
import hu.pogany.freshPotato.dto.response.StaffDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.Staff;
import hu.pogany.freshPotato.entity.StaffRole;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.repository.StaffRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StaffService {

    private final StaffRepository staffRepository;
    private final MovieRepository movieRepository;
    private final Mapper mapper;

    public StaffService(StaffRepository staffRepository, MovieRepository movieRepository, Mapper mapper) {
        this.staffRepository = staffRepository;
        this.movieRepository = movieRepository;
        this.mapper = mapper;
    }

    public StaffDto getById(int id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        List<Movie> playedMovies = movieRepository.findByStaffAndRole(staff.getId(), StaffRole.ACTOR);
        List<Movie> directedMovies = movieRepository.findByStaffAndRole(staff.getId(), StaffRole.DIRECTOR);

        List<SearchMovieDto> playedMovieDtos = mapper.toSearchMovieDtoList(playedMovies);
        List<SearchMovieDto> directedMovieDtos = mapper.toSearchMovieDtoList(directedMovies);

        StaffDto base = mapper.toStaffDto(staff);
        return StaffDto.builder()
                .id(base.id())
                .name(base.name())
                .gender(base.gender())
                .birthDay(base.birthDay())
                .birthCountry(base.birthCountry())
                .playedMovies(playedMovieDtos)
                .directedMovies(directedMovieDtos)
                .build();
    }

    public List<SearchStaffDto> getAll(int page, int size) {
        List<Staff> staff = staffRepository.findAll(PageRequest.of(page, size)).getContent();
        return mapper.toSearchStaffDtoList(staff);
    }
}

