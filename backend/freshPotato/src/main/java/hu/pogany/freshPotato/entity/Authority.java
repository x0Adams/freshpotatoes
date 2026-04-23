package hu.pogany.freshPotato.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "authorities", schema = "fresh_potatoes")
public class Authority {
    @EmbeddedId
    private AuthorityId id;

    public String getUsername() {
        return id.getUsername();
    }

    public Authorities getAuthority() {
        return id.getAuthority();
    }

    public AuthorityId getId() {
        return id;
    }

    public void setId(AuthorityId id) {
        this.id = id;
    }

    //TODO [Reverse Engineering] generate columns from DB
}