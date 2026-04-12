package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenderRepository extends JpaRepository<Gender, Integer> {
    Optional<Gender> findByName(String name);
}
