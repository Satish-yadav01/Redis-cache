package com.npst.spring_redis.service;

import com.npst.spring_redis.dto.SessionRequest;
import com.npst.spring_redis.dto.SessionResponse;

import java.util.List;
import java.util.Map;

/**
 * @author : Satish Yadav
 * @purpose :
 */
public interface KeyDbService {
    String saveUser(String id, SessionRequest sessionRequest);

    String saveAllUser(String id, List<SessionRequest> sessionRequest);

    Map<String, SessionResponse> getAllUsers(String id);

    SessionResponse getUserBySessionId(String id,String sessionId);

    SessionResponse updateUserBySessionId(String id, SessionRequest sessionRequest);

    String updateAllUsers(String id,List<SessionRequest> sessionrequest);

    String deleteUserBySessionId(String id,String sessionId);

    String deleteAllUsers(String id);
}
