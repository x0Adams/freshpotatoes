package hu.pogany.freshPotato.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StaffRoleInMovieId implements Serializable {
    private static final long serialVersionUID = 7573352992902781713L;
    @Lob
    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "movie", nullable = false)
    private Integer movie;

    @Column(name = "staff", nullable = false)
    private Integer staff;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getMovie() {
        return movie;
    }

    public void setMovie(Integer movie) {
        this.movie = movie;
    }

    public Integer getStaff() {
        return staff;
    }

    public void setStaff(Integer staff) {
        this.staff = staff;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffRoleInMovieId entity = (StaffRoleInMovieId) o;
        return Objects.equals(this.role, entity.role) &&
                Objects.equals(this.movie, entity.movie) &&
                Objects.equals(this.staff, entity.staff);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, movie, staff);
    }
}