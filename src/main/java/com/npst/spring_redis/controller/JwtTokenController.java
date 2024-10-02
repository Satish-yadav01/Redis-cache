package com.npst.spring_redis.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.npst.spring_redis.dto.Request;
import com.npst.spring_redis.entity.JwtToken;
import com.npst.spring_redis.service.JwtTokenService;

@RestController
@RequestMapping("/api/tokens")
@Slf4j
public class JwtTokenController {

    @Autowired
    private JwtTokenService jwtTokenService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    @PostMapping("/generate")
    public String generateTokens(@RequestParam int count, @RequestBody Request loginRequest) {
        try {
            for (int i = 0; i < count; i++) {
                executorService.submit(() -> {
                    jwtTokenService.createToken(loginRequest.getUsername(), loginRequest.getPassword());
                });
            }

        } catch (Exception e) {
            log.error("Exception occurred while creating tokens : {}",e.getMessage(), e);
            return "FAILED";
        }
        return "SUCCESS";
    }

    @GetMapping("/")
    public List<JwtToken> getAllTokens() {
        return jwtTokenService.getAllTokens();
    }

    @DeleteMapping("/{id}")
    public void logout(@PathVariable Long id) {
        Optional<JwtToken> token = jwtTokenService.getTokenById(id);
        String userName = jwtTokenService.extractSubject(token.toString());
        jwtTokenService.logout(userName);
    }
}
