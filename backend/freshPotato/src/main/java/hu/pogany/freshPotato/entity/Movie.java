package hu.pogany.freshPotato.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "movie", schema = "fresh_potato")
public class Movie{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name = "poster_path", nullable = false)
    private String posterPath;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Lob
    @Column(name = "youtube_movie", nullable = false)
    private String youtubeMovie;

    @Lob
    @Column(name = "google_knowledge_graph", nullable = false)
    private String googleKnowledgeGraph;

    @Column(name = "country_of_origin", nullable = false, length = 50)
    private String countryOfOrigin;

    @Lob
    @Column(name = "trailer", nullable = false)
    private String trailer;

    @ManyToMany
    @JoinTable(name = "genre_movie", joinColumns = {@JoinColumn(name = "movie")}, inverseJoinColumns = {@JoinColumn(name = "genre")})
    private Set<Genre> genres = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "acted", joinColumns = {@JoinColumn(name = "movie")}, inverseJoinColumns = {@JoinColumn(name = "actor")})
    private Set<Actor> actors = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "directed", joinColumns = {@JoinColumn(name = "movie")}, inverseJoinColumns = {@JoinColumn(name = "director")})
    private Set<Director> directors = new LinkedHashSet<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getGoogleKnowledgeGraph() {
        return googleKnowledgeGraph;
    }

    public void setGoogleKnowledgeGraph(String googleKnowledgeGraph) {
        this.googleKnowledgeGraph = googleKnowledgeGraph;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
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

    public Set<Actor> getActors() {
        return actors;
    }

    public void setActors(Set<Actor> actors) {
        this.actors = actors;
    }

    public Set<Director> getDirectors() {
        return directors;
    }

    public void setDirectors(Set<Director> directors) {
        this.directors = directors;
    }
}