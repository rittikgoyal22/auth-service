package com.etd.auth_service.dao;

import com.etd.auth_service.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepo extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TokenBlacklist tb WHERE tb.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

}
