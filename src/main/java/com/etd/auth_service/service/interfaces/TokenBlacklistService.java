package com.etd.auth_service.service.interfaces;

public interface TokenBlacklistService {

    void blacklistToken(String token);

    boolean isBlacklisted(String token);

}
