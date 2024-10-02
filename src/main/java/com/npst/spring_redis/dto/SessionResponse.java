package com.npst.spring_redis.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

import java.util.Date;

/**
 * @author : Satish Yadav
 * @purpose :
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@JsonTypeName("com.npst.spring_redis.dto.SessionResponse") // Optional, but can help with clarity
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,  // Include the class name as the type identifier
        include = JsonTypeInfo.As.PROPERTY,  // Include the type info as a property in the JSON
        property = "@class"  // Name of the property that will hold the class name
)
public class SessionResponse {
    private String sessionId;
    private String token;
    private String creationDate;
    private String expiryDate;
}
