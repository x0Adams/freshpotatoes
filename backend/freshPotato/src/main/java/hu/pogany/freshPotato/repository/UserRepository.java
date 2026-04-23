package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String userName);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
