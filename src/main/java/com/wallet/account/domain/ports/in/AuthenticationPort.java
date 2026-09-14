package com.wallet.account.domain.ports.in;

import com.wallet.account.infrastructure.adapters.in.security.AuthenticationResult;
import com.wallet.account.domain.core.Result;
import io.jsonwebtoken.Claims;

public interface AuthenticationPort {
    public Result<Claims, AuthenticationResult> validateToken(String token);
    public boolean isTokenExpired(Claims claims);
}
