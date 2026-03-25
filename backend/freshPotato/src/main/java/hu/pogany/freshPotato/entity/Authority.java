package hu.pogany.freshPotato.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "authorities", schema = "fresh_potatoes")
public class Authority {
    @EmbeddedId
    private AuthorityId id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "authority", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Authorities authority;

    public String getUsername() {
        return username;
    }

    public Authorities getAuthority() {
        return authority;
    }

    public void setAuthority(Authorities authority) {
        this.authority = authority;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AuthorityId getId() {
        return id;
    }

    public void setId(AuthorityId id) {
        this.id = id;
    }

    //TODO [Reverse Engineering] generate columns from DB
}