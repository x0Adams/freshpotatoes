package hu.pogany.freshPotato.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "user", schema = "fresh_potatoes")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "email", nullable = false, length = 75)
    private String email;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gender_id", nullable = false)
    private Gender gender;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "password_hash", nullable = false, length = 250)
    private String passwordHash;

    @ColumnDefault("current_timestamp()")
    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @OneToMany(mappedBy = "owner")
    private Set<Playlist> playlists = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Rate> rates = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<RefreshToken> refreshTokens = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Review> reviews = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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

    public Set<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Set<Playlist> playlists) {
        this.playlists = playlists;
    }

    public Set<Rate> getRates() {
        return rates;
    }

    public void setRates(Set<Rate> rates) {
        this.rates = rates;
    }

    public Set<RefreshToken> getRefreshTokens() {
        return refreshTokens;
    }

    public void setRefreshTokens(Set<RefreshToken> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

}