package hu.pogany.freshPotato.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "refresh_token", schema = "fresh_potato")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_uuid", nullable = false)
    private User userUuid;

    @Column(name = "token", nullable = false, length = 64)
    private String token;

    @ColumnDefault("current_timestamp()")
    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @ColumnDefault("'0000-00-00 00:00:00'")
    @Column(name = "expiration_date", nullable = false)
    private Instant expirationDate;

    @Column(name = "used", nullable = false)
    private Boolean used;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public User getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(User userUuid) {
        this.userUuid = userUuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

}