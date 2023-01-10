package com.jftse.server.core.service;

import com.jftse.entities.database.model.auth.AuthToken;

import java.util.List;

public interface AuthTokenService {
    AuthToken save(AuthToken authToken);

    void remove(AuthToken authToken);

    AuthToken findAuthToken(String token, Long timestamp, String accountName);

    AuthToken findAuthToken(String token);

    List<AuthToken> findAuthTokensByAccountName(String accountName);
}
