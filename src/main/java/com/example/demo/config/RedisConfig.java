package com.example.demo.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis configuration for the application.
 * This class defines the necessary beans for connecting to a Redis server,
 * using Lettuce as the client and Dotenv for host and port configuration.
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  private final Dotenv dotenv;

  /**
   * Creates and configures the Redis connection using Lettuce.
   * Reads the REDIS_HOST and REDIS_PORT environment variables from the .env file or environment.
   *
   * @return an instance of {@link LettuceConnectionFactory}
   *       connected to the specified Redis server.
   */
  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    String host = dotenv.get("REDIS_HOST", "localhost");
    int port = Integer.parseInt(dotenv.get("REDIS_PORT", "6379"));

    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    config.setHostName(host);
    config.setPort(port);
    return new LettuceConnectionFactory(config);
  }


  /**
   * Configures a {@link RedisTemplate} to work with String keys and values.
   * This template simplifies access to Redis using String as both key and value types.
   *
   * @param factory the Redis connection factory automatically injected by Spring.
   * @return a {@link RedisTemplate} configured with the Redis connection.
   */
  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    return template;
  }
}
