package entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "user")
public class User {
    @Id
    @Column(name = "uuid", nullable = false, length = 16)
    private String uuid;

    @Column(name = "email", nullable = false, length = 70)
    private String email;

    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @Column(name = "password_hash", nullable = false, length = 250)
    private String passwordHash;

    @ColumnDefault("current_timestamp()")
    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @ManyToMany(mappedBy = "users")
    private Set<Movie> movies = new LinkedHashSet<>();

    @OneToMany(mappedBy = "userUuid")
    private Set<RefreshToken> refreshTokens = new LinkedHashSet<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Set<Movie> getMovies() {
        return movies;
    }

    public void setMovies(Set<Movie> movies) {
        this.movies = movies;
    }

    public Set<RefreshToken> getRefreshTokens() {
        return refreshTokens;
    }

    public void setRefreshTokens(Set<RefreshToken> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

}