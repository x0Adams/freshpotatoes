package hu.notkulonme.DataTransferer.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "movie")
public class Movie {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "poster_path", nullable = true, length = 150)
    private String posterPath;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "release_date", nullable = true)
    private LocalDate releaseDate;

    @Column(name = "youtube_movie", nullable = true, length = 200)
    private String youtubeMovie;

    @Column(name = "trailer", nullable = true, length = 200)
    private String trailer;

    @ManyToMany
    private Set<Genre> genres = new LinkedHashSet<>();

    @ManyToMany
    private Set<Country> countries = new LinkedHashSet<>();

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

    public Set<Country> getCountries() {
        return countries;
    }

    public void setCountries(Set<Country> countries) {
        this.countries = countries;
    }

    public Set<StaffRoleInMovie> getStaffRoleInMovies() {
        return staffRoleInMovies;
    }

    public void setStaffRoleInMovies(Set<StaffRoleInMovie> staffRoleInMovies) {
        this.staffRoleInMovies = staffRoleInMovies;
    }

}