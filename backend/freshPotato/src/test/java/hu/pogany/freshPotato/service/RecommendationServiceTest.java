package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.config.RecommendationConfig;
import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private Mapper mapper;

    private RecommendationService recommendationService;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        recommendationService = new RecommendationService(
                new RecommendationConfig("http://localhost:8000", 10),
                movieRepository,
                mapper,
                builder
        );
    }

    @Test
    void getRecommendations_shouldMapMoviesInMicroserviceOrder() {
        server.expect(requestTo("http://localhost:8000/recommend/7?count=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"user_id\":7,\"requested\":10,\"movie_ids\":[2,1]}", MediaType.APPLICATION_JSON));

        Movie movie1 = createMovie(1, "Movie 1");
        Movie movie2 = createMovie(2, "Movie 2");

        when(movieRepository.findByIdIn(List.of(2, 1))).thenReturn(List.of(movie1, movie2));
        when(mapper.toSearchMovieDto(movie1)).thenReturn(new SearchMovieDto("1", "Movie 1", null, null, null));
        when(mapper.toSearchMovieDto(movie2)).thenReturn(new SearchMovieDto("2", "Movie 2", null, null, null));

        List<SearchMovieDto> result = recommendationService.getRecommendations(7);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).id());
        assertEquals("1", result.get(1).id());
        server.verify();
    }

    @Test
    void getRecommendations_shouldReturnEmptyList_whenRecommendationServiceReturns404() {
        server.expect(requestTo("http://localhost:8000/recommend/99?count=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        List<SearchMovieDto> result = recommendationService.getRecommendations(99);

        assertTrue(result.isEmpty());
        verifyNoInteractions(movieRepository, mapper);
        server.verify();
    }

    @Test
    void getRecommendations_shouldReturnEmptyList_whenRecommendationServiceReturns4xx() {
        server.expect(requestTo("http://localhost:8000/recommend/3?count=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST));

        List<SearchMovieDto> result = recommendationService.getRecommendations(3);

        assertTrue(result.isEmpty());
        verifyNoInteractions(movieRepository, mapper);
        server.verify();
    }

    @Test
    void getRecommendations_shouldReturnEmptyList_whenRecommendationServiceReturns5xx() {
        server.expect(requestTo("http://localhost:8000/recommend/5?count=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));

        List<SearchMovieDto> result = recommendationService.getRecommendations(5);

        assertTrue(result.isEmpty());
        verifyNoInteractions(movieRepository, mapper);
        server.verify();
    }

    private Movie createMovie(int id, String name) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setName(name);
        movie.setReleaseDate(LocalDate.of(2020, 1, 1));
        return movie;
    }
}
