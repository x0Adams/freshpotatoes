package hu.pogany.freshPotato.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "movie", schema = "fresh_potatoes", indexes = {@Index(name = "name",
        columnList = "name")})
public class Movie {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "poster_path", length = 150)
    private String posterPath;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "wikipedia_title", length = 250)
    private String wikipediaTitle;

    @Column(name = "youtube_movie", length = 200)
    private String youtubeMovie;

    @Column(name = "trailer", length = 200)
    private String trailer;

    @ManyToMany
    private Set<Genre> genres = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private Set<MovieInPlaylist> movieInPlaylists = new LinkedHashSet<>();

    @ManyToMany
    private Set<Country> countries = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private Set<Rate> rates = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private Set<Review> reviews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private Set<StaffRoleInMovie> staffRoleInMovies = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getWikipediaTitle() {
        return wikipediaTitle;
    }

    public void setWikipediaTitle(String wikipediaTitle) {
        this.wikipediaTitle = wikipediaTitle;
    }

    public String getYoutubeMovie() {
        return youtubeMovie;
    }

    public void setYoutubeMovie(String youtubeMovie) {
        this.youtubeMovie = youtubeMovie;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public Set<MovieInPlaylist> getMovieInPlaylists() {
        return movieInPlaylists;
    }

    public void setMovieInPlaylists(Set<MovieInPlaylist> movieInPlaylists) {
        this.movieInPlaylists = movieInPlaylists;
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public void setCountries(Set<Country> countries) {
        this.countries = countries;
    }

    public Set<Rate> getRates() {
        return rates;
    }

    public void setRates(Set<Rate> rates) {
        this.rates = rates;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

    public Set<StaffRoleInMovie> getStaffRoleInMovies() {
        return staffRoleInMovies;
    }

    public void setStaffRoleInMovies(Set<StaffRoleInMovie> staffRoleInMovies) {
        this.staffRoleInMovies = staffRoleInMovies;
    }

}