package hu.pogany.freshPotato.dto.response;

import hu.pogany.freshPotato.dto.playlist.response.PlaylistDetailsDto;
import hu.pogany.freshPotato.dto.rate.GenericRateDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Builder;

import java.util.List;

@Builder
public record UserDto(
        int id,
        String username,
        String email,
        String gender,
        int age,
        List<PlaylistDetailsDto> playlists,
        List<GenericRateDto<Integer>> ratedMovies,
        List<GenericRateDto<String>> reviewedMovies
        ) {
}
