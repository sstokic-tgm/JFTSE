package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.entities.database.repository.auth.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class AuthTokenService {
    private final AuthTokenRepository authTokenRepository;

    public AuthToken save(AuthToken authToken) {
        return authTokenRepository.save(authToken);
    }

    public void remove(AuthToken authToken) {
        authTokenRepository.deleteById(authToken.getId());
    }

    public AuthToken findAuthToken(String token, Long timestamp, String accountName) {
        Optional<AuthToken> optional = authTokenRepository.findAuthTokenByTokenAndLoginTimestampAndAccountName(token, timestamp, accountName);
        return optional.orElse(null);
    }

    public AuthToken findAuthToken(String token) {
        Optional<AuthToken> optional = authTokenRepository.findAuthTokenByToken(token);
        return optional.orElse(null);
    }

    public List<AuthToken> findAuthTokensByAccountName(String accountName) {
        return authTokenRepository.findAuthTokensByAccountName(accountName);
    }
}
