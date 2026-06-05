package com.etd.auth_service.controller;

import com.etd.auth_service.service.interfaces.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class BlacklistController {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistController.class);

    private final TokenBlacklistService tokenBlacklistService;

    public BlacklistController(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @GetMapping("/blacklist/check")
    public ResponseEntity<Boolean> isBlacklisted(@RequestParam("token") String token) {
        logger.info("Inside BlacklistController :: Checking blacklist status for token");
        return ResponseEntity.ok(tokenBlacklistService.isBlacklisted(token));
    }

}
