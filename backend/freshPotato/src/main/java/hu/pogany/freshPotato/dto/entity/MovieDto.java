package hu.pogany.freshPotato.dto.entity;

import hu.pogany.freshPotato.entity.Actor;
import hu.pogany.freshPotato.entity.Director;
import hu.pogany.freshPotato.entity.Genre;

import java.time.LocalDate;
import java.util.Set;

public record MovieDto(String id,
                       String name,
                       String posterPath,
                       Integer duration,
                       LocalDate releaseDate,
                       String youtubeMovie,
                       String googleKnowledgeGraph,
                       String countryOfOrigin,
                       String trailer,
                       Set<GenreDto> genres,
                       Set<MovieActorDto> actors,
                       Set<MovieDirectorDto> directors
                       ){
}
