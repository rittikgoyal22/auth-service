package com.etd.auth_service.service.classes;

import com.etd.auth_service.dao.TokenBlacklistRepo;
import com.etd.auth_service.entity.TokenBlacklist;
import com.etd.auth_service.service.interfaces.TokenBlacklistService;
import com.etd.auth_service.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);

    private final TokenBlacklistRepo tokenBlacklistRepo;
    private final JWTUtil jwtUtil;

    public TokenBlacklistServiceImpl(TokenBlacklistRepo tokenBlacklistRepo, JWTUtil jwtUtil) {
        this.tokenBlacklistRepo = tokenBlacklistRepo;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public void blacklistToken(String token) {
        logger.info("Inside TokenBlacklistServiceImpl :: blacklistToken");
        try {
            LocalDateTime expiry = jwtUtil.extractExpiration(token);
            tokenBlacklistRepo.save(TokenBlacklist.builder()
                    .token(token)
                    .expiryDate(expiry)
                    .build());
            logger.info("Inside TokenBlacklistServiceImpl :: Token blacklisted successfully");
        } catch (Exception e) {
            logger.warn("Inside TokenBlacklistServiceImpl :: Could not blacklist token: {}", e.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepo.existsByToken(token);
    }

}
