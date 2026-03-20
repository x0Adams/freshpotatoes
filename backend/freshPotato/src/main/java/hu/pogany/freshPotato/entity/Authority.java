package hu.pogany.freshPotato.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "authorities", schema = "fresh_potatoes")
public class Authority {
    @EmbeddedId
    private AuthorityId id;

    public AuthorityId getId() {
        return id;
    }

    public void setId(AuthorityId id) {
        this.id = id;
    }

    //TODO [Reverse Engineering] generate columns from DB
}