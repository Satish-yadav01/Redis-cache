//package com.npst.spring_redis.scheduler;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.interfaces.DecodedJWT;
//
//import com.npst.spring_redis.entity.JwtToken;
//import com.npst.spring_redis.repo.JwtTokenRepository;
//import com.npst.spring_redis.service.JwtTokenService;
//
//@Component
//public class JwtTokenScheduler {
//
//	@Autowired
//    private JwtTokenRepository jwtTokenRepository;
//
//    @Autowired
//    private JwtTokenService jwtTokenService;
//
//
//    @Scheduled(fixedRate = 60000) // Run every minute
//    public void pushToRedis() {
//    	System.out.println("Push scheduler running now...");
//        List<JwtToken> inactiveTokens = jwtTokenRepository.findByStatus(0);
//        jwtTokenService.pushToRedis(inactiveTokens);
//    }
//
////    @Scheduled(fixedRate = 60000) // Run every minute
////    public void extendSession() {
////    	System.out.println("Session scheduler running now...");
////    	 List<JwtToken> activeTokens = jwtTokenRepository.findByStatus(1);
////    	 System.out.println("inactive tokens list : " + activeTokens);
////    	 for(JwtToken token : activeTokens) {
////        jwtTokenService.extendSession(token);
////    	 }
////    }
//
//    // Pull odd keys every 3 minutes (180,000 milliseconds)
//    @Scheduled(fixedRate = 180000) // 3 minutes
//    public void pullOddKeysFromRedis() {
//        System.out.println("Pulling odd keys from Redis...");
//        jwtTokenService.pullOddKeysFromRedis();
//    }
//
//    // Pull even keys every 5 minutes (300,000 milliseconds)
//    @Scheduled(fixedRate = 300000) // 5 minutes
//    public void pullEvenKeysFromRedis() {
//        System.out.println("Pulling even keys from Redis...");
//        jwtTokenService.pullEvenKeysFromRedis();
//    }
//}
