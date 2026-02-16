package hu.freshpotatoes.pojos;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "movie", schema = "fresh_potatoes")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "poster_path", nullable = false, length = 150)
    private String posterPath;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "youtube_movie", nullable = false, length = 200)
    private String youtubeMovie;

    @Column(name = "google_knowledge_graph", nullable = false, length = 200)
    private String googleKnowledgeGraph;

    @Column(name = "trailer", nullable = false, length = 200)
    private String trailer;

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

    public String getGoogleKnowledgeGraph() {
        return googleKnowledgeGraph;
    }

    public void setGoogleKnowledgeGraph(String googleKnowledgeGraph) {
        this.googleKnowledgeGraph = googleKnowledgeGraph;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

}