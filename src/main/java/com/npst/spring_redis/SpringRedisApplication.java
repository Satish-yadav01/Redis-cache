package com.npst.spring_redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringRedisApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringRedisApplication.class, args);
		System.out.println("-----Application started---------");
	}

}
