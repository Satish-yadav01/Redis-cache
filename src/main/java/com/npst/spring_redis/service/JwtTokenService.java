package com.npst.spring_redis.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.RedisStringCommands;

import com.npst.spring_redis.entity.JwtToken;
import com.npst.spring_redis.repo.JwtTokenRepository;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class JwtTokenService {

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${spring.secret.key}")
    private String SECRET_KEY;

    @Value("${spring.token.expiration}")
    private long expiration; // in seconds
    
    @Value("${spring.token.buffer}")
    private long bufferTime;

    @Async // Enable asynchronous processing
    public String createToken(String userName, String password) {
        System.out.println("Entered createToken() method");
        
        long startTime = System.currentTimeMillis(); // Start timing
        String jwtToken="";
        try {
            // Generate JWT token
            jwtToken = generateJwtToken(userName);

//            // Save token entity and send to Redis asynchronously
//            saveTokenToDatabaseAndRedis(jwtToken,userName);
            
            long endTime = System.currentTimeMillis(); // End timing
            System.out.println("Time taken to produce and store token in Redis: " + (endTime - startTime) + " ms");

        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception as needed
        }
        return jwtToken;
    }

    
    // every 1 min scheduler will call this method 
//    public void extendSession(JwtToken token) {
//    	System.out.println("Entered extend session() method ");
//    	String userName = extractSubject(token.getJwtToken());
//        String tokenKey = "user:" + userName + ":token";
//        String jwtToken = redisTemplate.opsForValue().get(tokenKey);
//        
//        System.out.println("Extending session for the token " + jwtToken);
//        Long ttl = redisTemplate.getExpire(tokenKey);
//        System.out.println("Expiry of token before extending.... " + ttl);
//        if (jwtToken != null && !isTokenExpired(jwtToken)) {
//            // Extend the session by resetting the expiration time
//            redisTemplate.opsForValue().set(tokenKey, jwtToken, expiration, TimeUnit.SECONDS);
//            Long ttle = redisTemplate.getExpire(tokenKey);
//            System.out.println("Expiry of token after extending.... " + ttl);
//        } else {
//        	redisTemplate.delete(jwtToken);
//        }
//    }

    private String generateJwtToken(String userName) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Instant expirationInstant = now.plusSeconds(expiration);
        Date expirationDate = Date.from(expirationInstant);

        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private void saveTokenToDatabaseAndRedis(String jwtToken, String userName) {
        JwtToken tokenEntity = new JwtToken();
        tokenEntity.setJwtToken(jwtToken);
        tokenEntity.setStatus(0); // Initially inactive
        jwtTokenRepository.save(tokenEntity);
        
        String tokenKey = "user_" + userName + "_token";
        redisTemplate.opsForValue().set(tokenKey, jwtToken, expiration, TimeUnit.SECONDS);
    }

    private Key getSignInKey() {
    	byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

//    private boolean isTokenExpired(String token) {
//    	
//    	Instant now = Instant.now();
//    	
//        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
//       
//        Instant expirationTime = claims.getExpiration().toInstant();
//        
//        if (now.isAfter(expirationTime)) {
//            return false; // Token is already expired
//        }
//        
//        Instant remainingTime = expirationTime.minusSeconds(bufferTime);
//
//        return now.isAfter(bufferTime) && now.isBefore(expirationTime);
//        //bufferTime
//        //return claims.getExpiration().before(Date.from(Instant.now()));
//    }

    
    @Async // Enable asynchronous processing
    public void pushToRedis(List<JwtToken> tokens) {
        System.out.println("Inside pushToRedis()");
        long startTime = System.currentTimeMillis();

        // Save tokens to Redis using pipeline
        saveTokensToRedis(tokens);

        // Update token status in the database
        updateTokenStatus(tokens);

        long endTime = System.currentTimeMillis(); // End timing
        System.out.println("Time taken to push tokens to Redis: " + (endTime - startTime) + " ms");
    }

    private void saveTokensToRedis(List<JwtToken> tokens) {
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            connection.openPipeline();
            for (JwtToken token : tokens) {
                connection.set(longToBytes(token.getId()), token.getJwtToken().getBytes(StandardCharsets.UTF_8), Expiration.seconds(expiration),RedisStringCommands.SetOption.UPSERT);
            }
            connection.closePipeline();
        }
    }
    
    private byte[] longToBytes(Long value) {
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }

    private void updateTokenStatus(List<JwtToken> tokens) {
        for (JwtToken token : tokens) {
            token.setStatus(1);
        }
        jwtTokenRepository.saveAll(tokens);
    }
    
    public void pullFromRedis() {
        System.out.println("Inside pull from redis()");
        long startTime = System.currentTimeMillis(); // Start timing
        
        // Use a more efficient scan method
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("*").build());

            // Iterate over the scanned keys and retrieve their values
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                String value = redisTemplate.opsForValue().get(new String(key, StandardCharsets.UTF_8));
                System.out.println("Key: " + new String(key, StandardCharsets.UTF_8) + ", Value: " + value);
            }
        }

        long endTime = System.currentTimeMillis(); // End timing
        System.out.println("Time taken to pull tokens from Redis: " + (endTime - startTime) + " ms");
    }
    
    public void pullOddKeysFromRedis() {
        System.out.println("Inside pull odd keys from redis()");
        long startTime = System.currentTimeMillis(); // Start timing

        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("*").build());

            // Iterate over the scanned keys and retrieve their values
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                String keyString = new String(key, StandardCharsets.UTF_8);
                
                // Check if the key is odd (assuming keys are numeric strings)
                if (isOddKey(keyString)) {
                    String value = redisTemplate.opsForValue().get(keyString);
                    System.out.println("Odd Key: " + keyString + ", Value: " + value);
                }
            }
        }

        long endTime = System.currentTimeMillis(); // End timing
        System.out.println("Time taken to pull odd keys from Redis: " + (endTime - startTime) + " ms");
    }
    
    // Method to pull even keys from Redis
    public void pullEvenKeysFromRedis() {
        System.out.println("Inside pull even keys from redis()");
        long startTime = System.currentTimeMillis(); // Start timing

        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("*").build());

            // Iterate over the scanned keys and retrieve their values
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                String keyString = new String(key, StandardCharsets.UTF_8);
                
                // Check if the key is even (assuming keys are numeric strings)
                if (isEvenKey(keyString)) {
                    String value = redisTemplate.opsForValue().get(keyString);
                    System.out.println("Even Key: " + keyString + ", Value: " + value);
                }
            }
        }

        long endTime = System.currentTimeMillis(); // End timing
        System.out.println("Time taken to pull even keys from Redis: " + (endTime - startTime) + " ms");
    }
    
    // Helper method to determine if a key is odd
    private boolean isOddKey(String key) {
        try {
            return Integer.parseInt(key) % 2 != 0; // Check if the key is odd
        } catch (NumberFormatException e) {
            return false; // Handle non-numeric keys
        }
    }

    // Helper method to determine if a key is even
    private boolean isEvenKey(String key) {
        try {
            return Integer.parseInt(key) % 2 == 0; // Check if the key is even
        } catch (NumberFormatException e) {
            return false; // Handle non-numeric keys
        }
    }
    
    public List<JwtToken> getAllTokens() {
        return jwtTokenRepository.findAll();
    }
    
    public Optional<JwtToken> getTokenById(Long id) {
    	return jwtTokenRepository.findById(id);
    }
    
    public void logout(String userName) {
        String tokenKey = "user:" + userName + ":token";
        redisTemplate.delete(tokenKey);
    }
    
    public String extractSubject(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // This returns the subject
    }
        
}


