package hu.pogany.freshPotato.dao;

import hu.pogany.freshPotato.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActorRepository extends JpaRepository<Actor, Integer> {
}
