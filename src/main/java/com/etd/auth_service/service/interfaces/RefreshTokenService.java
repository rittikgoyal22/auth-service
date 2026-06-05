package com.etd.auth_service.service.interfaces;

import com.etd.auth_service.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String emailAddress);

    RefreshToken verifyExpiration(RefreshToken refreshToken);

    void deleteByEmailAddress(String emailAddress);

    RefreshToken findByToken(String token);

}
