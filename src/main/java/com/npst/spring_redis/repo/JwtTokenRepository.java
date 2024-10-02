package com.npst.spring_redis.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.npst.spring_redis.entity.JwtToken;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
	
	List<JwtToken> findByStatus(int status);
	
}
