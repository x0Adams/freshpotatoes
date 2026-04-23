package hu.pogany.freshPotato.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "staff_role_in_movie", schema = "fresh_potatoes", indexes = {
        @Index(name = "movie",
                columnList = "movie_id"),
        @Index(name = "staff",
                columnList = "staff_id")})
public class StaffRoleInMovie {
    @EmbeddedId
    private StaffRoleInMovieId id;

    @MapsId("movie")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @MapsId("staff")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    public StaffRoleInMovieId getId() {
        return id;
    }

    public void setId(StaffRoleInMovieId id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

}
