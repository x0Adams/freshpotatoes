package hu.pogany.freshPotato.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "movie", schema = "fresh_potato")
public class Movie implements UuidPrimaryKey{
    @Id
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

}