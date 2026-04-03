package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.RateDto;
import hu.pogany.freshPotato.dto.RateRequestDto;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.service.RateService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rate")
public class RateController {
    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @PostMapping("/secure/rate")
    public ResponseEntity<String> rateMovie(@RequestBody @Valid RateRequestDto rate, @AuthenticationPrincipal Jwt jwt) {
        ResponseEntity<String> response;
        try {
            rateService.saveRating(
                    RateDto.builder()
                            .userId(Integer.parseInt(jwt.getClaim("uid")))
                            .movieId(rate.movieId())
                            .rating(rate.rate())
                            .build()
                            );
            response = ResponseEntity.ok("Movie rating was saved");
        } catch (EntityNotFoundException e) {
            response = ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (EntityExistsException e) {
            response = ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return response;
    }


}
