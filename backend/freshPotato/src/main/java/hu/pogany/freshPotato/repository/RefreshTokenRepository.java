package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.RefreshToken;
import hu.pogany.freshPotato.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    @Modifying
    @Query(value = "update refresh_token set used = true where token = :hash and used = false", nativeQuery = true)
    int updateUsedToFalse(@Param("hash") String hash);

    Optional<RefreshToken> findByToken(String hash);
}
