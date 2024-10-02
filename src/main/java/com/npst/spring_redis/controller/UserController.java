package com.npst.spring_redis.controller;

/**
 * @author : Satish Yadav
 * @purpose :
 */
import com.npst.spring_redis.dto.SessionRequest;
import com.npst.spring_redis.dto.SessionResponse;
import com.npst.spring_redis.service.KeyDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private KeyDbService keyDbService;

    // Endpoint to save a user HashMap in KeyDB
    @PostMapping("/{id}")
    public String saveUser(@PathVariable String id, @RequestBody SessionRequest sessionRequest) {
        return keyDbService.saveUser(id, sessionRequest);
    }

    // Endpoint to get a user HashMap from KeyDB
    @GetMapping("/{id}")
    public Map<String, SessionResponse> getUser(@PathVariable String id) {
        return keyDbService.getUser(id);
    }
}

