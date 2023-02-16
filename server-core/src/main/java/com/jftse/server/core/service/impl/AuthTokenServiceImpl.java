package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.entities.database.repository.auth.AuthTokenRepository;
import com.jftse.server.core.service.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class AuthTokenServiceImpl implements AuthTokenService {
    private final AuthTokenRepository authTokenRepository;

    @Override
    public AuthToken save(AuthToken authToken) {
        return authTokenRepository.save(authToken);
    }

    @Override
    public void remove(AuthToken authToken) {
        authTokenRepository.deleteById(authToken.getId());
    }

    @Override
    public AuthToken findAuthToken(String token, Long timestamp, String accountName) {
        Optional<AuthToken> optional = authTokenRepository.findAuthTokenByTokenAndLoginTimestampAndAccountName(token, timestamp, accountName);
        return optional.orElse(null);
    }

    @Override
    public AuthToken findAuthToken(String token) {
        Optional<AuthToken> optional = authTokenRepository.findAuthTokenByToken(token);
        return optional.orElse(null);
    }

    @Override
    public List<AuthToken> findAuthTokensByAccountName(String accountName) {
        return authTokenRepository.findAuthTokensByAccountName(accountName);
    }
}
