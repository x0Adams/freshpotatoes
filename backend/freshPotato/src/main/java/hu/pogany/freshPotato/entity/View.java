package hu.pogany.freshPotato.entity;

import hu.pogany.freshPotato.dto.MovieDto;
import jakarta.persistence.*;

@Entity
@Table(name = "view", schema = "fresh_potatoes", indexes = {@Index(name = "movie_view",
        columnList = "movie_id")})
public class View {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne
    private Movie movie;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}