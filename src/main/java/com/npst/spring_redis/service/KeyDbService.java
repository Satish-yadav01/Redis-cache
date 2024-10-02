package com.npst.spring_redis.service;

import com.npst.spring_redis.dto.SessionRequest;
import com.npst.spring_redis.dto.SessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.PostConstruct;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hibernate.type.descriptor.java.JdbcDateJavaType.DATE_FORMAT;

@Service
@Slf4j
public class KeyDbService {

    private static final String KEY = "SESSION";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"; // ISO 8601 format
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT); // Reusable date formatter


    @Autowired
    private RedisTemplate<String, HashMap<String, SessionResponse>> redisTemplate;
    private final JwtTokenService jwtTokenService;

    private HashOperations<String, String, SessionResponse> hashOperations;

    public KeyDbService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostConstruct
    public void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    public String saveUser(String id, SessionRequest sessionRequest) {
        String uniqueSessionId = sessionRequest.getSessionId();
        try {
            // Get the current date (creation time)
            Date currentDate = new Date();

            // Calculate expiry date (5 minutes later)
            Date expiryDate = new Date(currentDate.getTime() + TimeUnit.MINUTES.toMillis(5));

            Map<String, SessionResponse> users = getUser(id);
            if (users.containsKey(uniqueSessionId)) {
                SessionResponse sessionResponse = users.get(uniqueSessionId);
                String creationDateStr = sessionResponse.getCreationDate();

                // Parse the creationDate string into a Date object
                Date creationDate = dateFormat.parse(creationDateStr);

                // Calculate the time difference between the current date and the creation date
                long timeDifferenceMillis = currentDate.getTime() - creationDate.getTime();
                long timeDifferenceMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);

                // Check if more than 5 minutes have passed
                if (timeDifferenceMinutes < 5) {
                    System.out.println("Session is still within the 5-minute window.");
                    return "No need to update session, sessionId: " + uniqueSessionId;
                }
            }

            System.out.println("More than 5 minutes have passed since session creation.");
            String token = jwtTokenService.createToken(sessionRequest.getUsername(), sessionRequest.getPassword());

            // Use the builder to set sessionId, token, creationDate, and expiryDate (formatted)
            SessionResponse sessionResponse = SessionResponse.builder()
                    .sessionId(uniqueSessionId)
                    .token(token)
                    .creationDate(dateFormat.format(currentDate))  // Store the formatted creation date
                    .expiryDate(dateFormat.format(expiryDate))    // Store the formatted expiry date
                    .build();

            // Save the session map
            HashMap<String, SessionResponse> sessionMap = new HashMap<>();
            sessionMap.put(uniqueSessionId, sessionResponse);
            hashOperations.putAll(KEY + "_" + id, sessionMap);
        } catch (Exception e) {
            log.error("error is : {}", e.getMessage(), e);
        }
        return "User data saved with sessionId: " + uniqueSessionId;
    }

    // Method to retrieve a HashMap from KeyDB
    public Map<String, SessionResponse> getUser(String id) {
        Map<String, SessionResponse> entries = hashOperations.entries(KEY + "_" + id);
        return entries;
    }


}

