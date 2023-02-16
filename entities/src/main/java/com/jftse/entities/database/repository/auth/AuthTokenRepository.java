package com.jftse.entities.database.repository.auth;

import com.jftse.entities.database.model.auth.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findAuthTokenByTokenAndLoginTimestampAndAccountName(String token, Long timestamp, String accountName);
    Optional<AuthToken> findAuthTokenByToken(String token);
    List<AuthToken> findAuthTokensByAccountName(String accountName);
}
