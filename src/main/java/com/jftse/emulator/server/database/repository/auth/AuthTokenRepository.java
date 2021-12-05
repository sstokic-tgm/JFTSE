package com.jftse.emulator.server.database.repository.auth;

import com.jftse.emulator.server.database.model.auth.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findAuthTokenByTokenAndLoginTimestampAndAccountName(String token, Long timestamp, String accountName);
    Optional<AuthToken> findAuthTokenByToken(String token);
    Optional<AuthToken> findAuthTokenByAccountName(String accountName);
}
