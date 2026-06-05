package com.etd.auth_service.config;

import com.etd.auth_service.dao.TokenBlacklistRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final TokenBlacklistRepo tokenBlacklistRepo;

    public DataInitializer(TokenBlacklistRepo tokenBlacklistRepo) {
        this.tokenBlacklistRepo = tokenBlacklistRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        cleanupExpiredBlacklistTokens();
    }

    private void cleanupExpiredBlacklistTokens() {
        logger.info("DataInitializer :: Cleaning up expired blacklisted tokens");
        tokenBlacklistRepo.deleteExpiredTokens(LocalDateTime.now());
    }

}
