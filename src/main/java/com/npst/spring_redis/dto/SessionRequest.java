package com.npst.spring_redis.dto;

import lombok.*;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author : Satish Yadav
 * @purpose :
 */
@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SessionRequest {
    private String sessionId;
    private String username;
    private String password;
}
