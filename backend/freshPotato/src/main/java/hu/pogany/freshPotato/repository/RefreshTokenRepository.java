package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    @Modifying
    @Query(value = "update refresh_token set used = true where user_id = :uid and token = :hash and used = false", nativeQuery = true)
    int updateUsedToFalse(@Param("uid") int uid, @Param("hash") String hash);
}
