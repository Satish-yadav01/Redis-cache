package com.npst.spring_redis.service.impl;

import com.npst.spring_redis.dto.SessionRequest;
import com.npst.spring_redis.dto.SessionResponse;
import com.npst.spring_redis.service.JwtTokenService;
import com.npst.spring_redis.service.KeyDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.PostConstruct;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class KeyDbServiceImpl implements KeyDbService {

    private static final String KEY = "SESSION";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"; // ISO 8601 format
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);


    @Autowired
    private RedisTemplate<String, HashMap<String, SessionResponse>> redisTemplate;
    private final JwtTokenService jwtTokenService;

    private HashOperations<String, String, SessionResponse> hashOperations;

    public KeyDbServiceImpl(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostConstruct
    public void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public String saveUser(String id, SessionRequest sessionRequest) {
        String uniqueSessionId = sessionRequest.getSessionId();
        String sessionKey = KEY + "_" + id;
        try {
            // Get the current date (creation time)
            Date currentDate = new Date();

            // Calculate expiry date (5 minutes later)
            Date expiryDate = new Date(currentDate.getTime() + TimeUnit.MINUTES.toMillis(5));

            Map<String, SessionResponse> users = getAllUsers(id);
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
                    .creationDate(dateFormat.format(currentDate))
                    .expiryDate(dateFormat.format(expiryDate))
                    .build();

            // Save the session map
            HashMap<String, SessionResponse> sessionMap = new HashMap<>();
            sessionMap.put(uniqueSessionId, sessionResponse);
            hashOperations.putAll(sessionKey, sessionMap);

//            if(redisTemplate.hasKey(sessionKey)){
//                redisTemplate.expire(sessionKey,5, TimeUnit.MINUTES);
//            }

        } catch (Exception e) {
            log.error("error is : {}", e.getMessage(), e);
        }
        return "User data saved with sessionId: " + uniqueSessionId;
    }


    @Override
    public String saveAllUser(String id, List<SessionRequest> sessionRequest) {
        String sessionKey = KEY + "_" + id;
        Date currentDate = new Date();
        Date expiryDate = new Date(currentDate.getTime() + TimeUnit.MINUTES.toMillis(5));
        HashMap<String, SessionResponse> sessionMap = new HashMap<>();

        try {
            for (SessionRequest request : sessionRequest) {
                SessionResponse sessionResponse = SessionResponse.builder()
                        .sessionId(request.getSessionId())
                        .token(jwtTokenService.createToken(request.getUsername(), request.getPassword()))
                        .creationDate(dateFormat.format(currentDate))
                        .expiryDate(dateFormat.format(expiryDate))
                        .build();

                sessionMap.put(request.getSessionId(), sessionResponse);
                hashOperations.putAll(sessionKey, sessionMap);
            }

            redisTemplate.expire(sessionKey, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("error is : {}", e.getMessage(), e);
            return "Unable to save User";
        }

        return "Saved All Users with sessionId: " + id;
    }

    @Override
    public Map<String, SessionResponse> getAllUsers(String id) {
        Map<String, SessionResponse> entries = hashOperations.entries(KEY + "_" + id);
        return entries;
    }

    @Override
    public SessionResponse getUserBySessionId(String id, String sessionId) {
        SessionResponse sessionResponse = new SessionResponse();
        try {
            sessionResponse = hashOperations.entries(KEY + "_" + id).get(sessionId);
            if (sessionResponse == null) {
                return null;
            }
        } catch (Exception e) {
            log.error("error is : {}", e.getMessage(), e);
        }

        return sessionResponse;
    }

    @Override
    public SessionResponse updateUserBySessionId(String id, SessionRequest sessionRequest) {
        Date currentDate = new Date();
        Date expiryDate = new Date(currentDate.getTime() + TimeUnit.MINUTES.toMillis(5));
        SessionResponse sessionResponse = new SessionResponse();
        String sessionKey = KEY + "_" + id;

        try {
            String uniqueSessionId = sessionRequest.getSessionId();
            Map<String, SessionResponse> users = getAllUsers(id);
            if (!users.containsKey(uniqueSessionId)) {
                log.error("Session not found for sessionId: {}", uniqueSessionId);
                return null;
            }

            sessionResponse.setSessionId(uniqueSessionId);
            sessionResponse.setToken(jwtTokenService.createToken(sessionRequest.getUsername(), sessionRequest.getPassword()));
            sessionResponse.setCreationDate(dateFormat.format(currentDate));
            sessionResponse.setExpiryDate(dateFormat.format(expiryDate));

            // Save the session map
            users.put(uniqueSessionId, sessionResponse);
            hashOperations.putAll(sessionKey, users);

        } catch (Exception e) {
            log.error("error is : {}", e.getMessage(), e);
        }

        return sessionResponse;
    }

    @Override
    public String updateAllUsers(String id, List<SessionRequest> sessionRequest) {
        String sessionKey = KEY + "_" + id;
        Date currentDate = new Date();
        Date expiryDate = new Date(currentDate.getTime() + TimeUnit.MINUTES.toMillis(5));
        HashMap<String, SessionResponse> sessionMap = new HashMap<>();

        try {
            for (SessionRequest request : sessionRequest) {
                SessionResponse sessionResponse = SessionResponse.builder()
                        .sessionId(request.getSessionId())
                        .token(jwtTokenService.createToken(request.getUsername(), request.getPassword()))
                        .creationDate(dateFormat.format(currentDate))
                        .expiryDate(dateFormat.format(expiryDate))
                        .build();

                sessionMap.put(request.getSessionId(), sessionResponse);
                hashOperations.putAll(sessionKey, sessionMap);
            }
        } catch (Exception e) {
            log.error("error is : {}", e.getMessage(), e);
            return "Unable to save All Users";
        }
        return "All user saved with SESSION key : " + sessionKey;
    }

    @Override
    public String deleteUserBySessionId(String id, String sessionId) {
        String sessionKey = KEY + "_" + id;
        SessionResponse removedSession = new SessionResponse();
        try {
            // Retrieve the map from KeyDB
            Map<String, SessionResponse> sessionMap = getAllUsers(id);

            // Remove the sessionId from the map
            removedSession = sessionMap.remove(sessionId);

            if (removedSession != null) {
                hashOperations.delete(sessionKey, sessionId);
            } else {
                return "Session ID not found.";
            }

        } catch (Exception e) {
            log.error("error is : {}", e.getMessage(), e);
            return "Unable to delete User";
        }
        return "user deleted with sessionId: " + removedSession.getSessionId();
    }

    @Override
    public String deleteAllUsers(String id) {
        String sessionKey = KEY + "_" + id;
        try {
            redisTemplate.delete(sessionKey);
        } catch (Exception e) {
            log.error("error is : {}", e.getMessage(), e);
            return "Unable to delete User";
        }
        return "user deleted with sessionId: " + id;
    }


}

