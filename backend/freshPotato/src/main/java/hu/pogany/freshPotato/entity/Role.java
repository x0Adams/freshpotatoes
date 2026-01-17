package hu.pogany.freshPotato.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role", schema = "fresh_potato")
public class Role {
    @Id
    @Column(name = "role", nullable = false, length = 30)
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    //TODO [Reverse Engineering] generate columns from DB
}