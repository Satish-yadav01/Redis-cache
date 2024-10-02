package com.npst.spring_redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.npst.spring_redis.dto.SessionResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import java.util.HashMap;

import com.fasterxml.jackson.core.type.TypeReference;

@Configuration
public class RedisConfig {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(6379);
        factory.getPoolConfig().setMaxTotal(200);   // Maximum number of connections
        factory.getPoolConfig().setMaxIdle(100);   // Maximum idle connections
        factory.getPoolConfig().setMinIdle(50);   // Minimum idle connections
        return factory;
    }

    @Bean
    public RedisTemplate<Long, String> redisTemplate() {
        RedisTemplate<Long, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new GenericToStringSerializer<>(Long.class)); // Use GenericToStringSerializer for Long keys
        template.setValueSerializer(new StringRedisSerializer()); // Use StringRedisSerializer for String values
        return template;
    }

//	@Bean
//	@Primary
//	public RedisTemplate<String, Object> redisTemplate2(RedisConnectionFactory connectionFactory) {
////		RedisTemplate<String, Object> template = new RedisTemplate<>();
////		template.setConnectionFactory(connectionFactory);
////		template.setKeySerializer(new StringRedisSerializer()); // serialize keys as Strings
////		template.setHashKeySerializer(new StringRedisSerializer());
////		template.setValueSerializer(new StringRedisSerializer());
////		template.setHashValueSerializer(new StringRedisSerializer());
////		return template;
//
//		RedisTemplate<String, Object> template = new RedisTemplate<>();
//		template.setConnectionFactory(connectionFactory);
//
//		// Set up custom serializer for keys
//		StringRedisSerializer stringSerializer = new StringRedisSerializer();
//		template.setKeySerializer(stringSerializer);
//		template.setHashKeySerializer(stringSerializer);
//
//		// Configure Jackson serializer for the values (to store objects as JSON)
//		Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
//
//		// Create ObjectMapper and configure it
//		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder().build(), ObjectMapper.DefaultTyping.NON_FINAL);
//
//		// Set the ObjectMapper directly in the serializer
//		jacksonSerializer.setObjectMapper(objectMapper);
//
//		template.setValueSerializer(jacksonSerializer);
//		template.setHashValueSerializer(jacksonSerializer);
//
//		return template;
//	}

    @Bean
    @Primary
    public RedisTemplate<String, HashMap<String, SessionResponse>> redisTemplateForHashMap(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, HashMap<String, SessionResponse>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Set up custom serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Configure Jackson serializer for HashMap
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // Create ObjectMapper and configure it
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(SessionResponse.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // Handle dates or other necessary configurations
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Set ObjectMapper in serializer
        jacksonSerializer.setObjectMapper(objectMapper);

        // Set the Jackson serializer for values
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        return template;
    }

}
