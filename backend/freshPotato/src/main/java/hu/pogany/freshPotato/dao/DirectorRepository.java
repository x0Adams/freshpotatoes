package hu.pogany.freshPotato.dao;

import hu.pogany.freshPotato.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectorRepository extends JpaRepository<Director, Integer> {
}
