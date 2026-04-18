package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.rate.DeleteRateDto;
import hu.pogany.freshPotato.dto.rate.DeleteRateRequestDto;
import hu.pogany.freshPotato.dto.rate.GenericRateDto;
import hu.pogany.freshPotato.dto.rate.GenericRateRequestDto;
import hu.pogany.freshPotato.service.AbstractRateService;
import hu.pogany.freshPotato.service.JwtService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;


public abstract class AbstractRateController<T, S> {
    private final AbstractRateService<T, S> rateService;

    public AbstractRateController(AbstractRateService<T, S> rateService) {
        this.rateService = rateService;
    }


    public ResponseEntity<String> rateMovie(GenericRateRequestDto<T> rate, Jwt jwt) {
        ResponseEntity<String> response;
        try {
            rateService.saveRating(
                    GenericRateDto.<T>builder()
                            .userId(getUserId(jwt))
                            .movieId(rate.movieId())
                            .rating(rate.rate())
                            .build()
            );
            response = ResponseEntity.ok("Movie rating was saved");
        } catch (EntityNotFoundException e) {
            response = ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }

        return response;
    }

    public void deleteRate(DeleteRateRequestDto deleteRateRequestDto, Jwt jwt) {
        rateService.deleteRating(getUserId(jwt), deleteRateRequestDto.movieId());
    }

    public void adminDeleteRate(DeleteRateDto dto) {
        rateService.deleteRating(dto);

    }

    public int getUserId(Jwt jwt) {
        return JwtService.getUserId(jwt);
    }

    public List<GenericRateDto<T>> getAllByUser(int userid) {
        return rateService.getAllByUser(userid);
    }

    public List<GenericRateDto<T>> getAllByMovie(int movieId) {
        return rateService.getAllByMovie(movieId);
    }
}
