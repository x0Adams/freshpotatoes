package hu.freshPotatoes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AuthorityId implements Serializable {
    private static final long serialVersionUID = -5498744487188986099L;
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "authority", nullable = false, length = 30)
    private String authority;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorityId entity = (AuthorityId) o;
        return Objects.equals(this.username, entity.username) &&
                Objects.equals(this.authority, entity.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, authority);
    }
}