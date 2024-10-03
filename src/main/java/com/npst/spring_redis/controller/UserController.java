package com.npst.spring_redis.controller;
import com.npst.spring_redis.dto.SessionRequest;
import com.npst.spring_redis.dto.SessionResponse;
import com.npst.spring_redis.service.KeyDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * @author : Satish Yadav
 * @purpose :
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private KeyDbService keyDbService;

    // Endpoint to save a user HashMap in KeyDB
    @PostMapping("/saveuser/{id}")
    public String saveUser(@PathVariable String id, @RequestBody SessionRequest sessionRequest) {
        return keyDbService.saveUser(id, sessionRequest);
    }

    @PostMapping("/savealluser/{id}")
    public String saveAllUser(@PathVariable String id,@RequestBody List<SessionRequest> sessionRequests) {
        return keyDbService.saveAllUser(id, sessionRequests);
    }

    // Endpoint to get a user HashMap from KeyDB
    @GetMapping("/getalluser/{id}")
    public Map<String, SessionResponse> getAllUser(@PathVariable String id) {
        return keyDbService.getAllUsers(id);
    }

    @GetMapping("/getuser/{id}/{sessionId}")
    public SessionResponse getUserBySessionId(@PathVariable String id,
                                              @PathVariable String sessionId) {
        return keyDbService.getUserBySessionId(id,sessionId);
    }

    @PutMapping("/updateuser/{id}")
    public SessionResponse updateUserBySessionId(@PathVariable String id,
                                              @RequestBody SessionRequest sessionRequest) {
        return keyDbService.updateUserBySessionId(id,sessionRequest);
    }

    @PutMapping("/updatealluser/{id}")
    public String updateAllUser(@PathVariable String id,@RequestBody List<SessionRequest> sessionRequests){
        return keyDbService.updateAllUsers(id,sessionRequests);
    }

    @DeleteMapping("/deleteuser/{id}/{sessionId}")
    public String deleteUser(@PathVariable String id,@PathVariable String sessionId) {
        return keyDbService.deleteUserBySessionId(id,sessionId);
    }

    @DeleteMapping("/deletealluser/{id}")
    public String deleteAllUsers(@PathVariable String id) {
        return keyDbService.deleteAllUsers(id);
    }

}

