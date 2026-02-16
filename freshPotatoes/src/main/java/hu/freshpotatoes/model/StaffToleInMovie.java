package hu.freshpotatoes.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "staff_tole_in_movie", schema = "fresh_potatoes")
public class StaffToleInMovie {
    @EmbeddedId
    private StaffToleInMovieId id;

    @MapsId("role")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "role", nullable = false)
    private Role role;

    @MapsId("movie")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "movie", nullable = false)
    private Movie movie;

    @MapsId("staff")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "staff", nullable = false)
    private Staff staff;

    public StaffToleInMovieId getId() {
        return id;
    }

    public void setId(StaffToleInMovieId id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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