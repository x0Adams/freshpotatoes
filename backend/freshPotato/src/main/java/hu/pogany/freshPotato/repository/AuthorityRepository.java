package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Authorities;
import hu.pogany.freshPotato.entity.Authority;
import hu.pogany.freshPotato.entity.AuthorityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, AuthorityId> {
    List<Authorities> findByUsername(String username);
}
