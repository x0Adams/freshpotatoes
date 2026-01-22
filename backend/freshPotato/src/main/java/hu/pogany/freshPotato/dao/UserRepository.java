package hu.pogany.freshPotato.dao;

import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    User findByUserName(String userName);
    @Query("SELECT count(token) from RefreshToken token where token.uuid = :id and token.used = false")
    long countActiveTokens(@Param("target") String id);
}
