package com.izhengyin.springcloud.gateway.base.config.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;
import java.util.Objects;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 09:52
 */
@Slf4j
public class RedisConnectionFactory {

    /**
     * 获取 {@link ReactiveRedisConnectionFactory}
     * @param redisProperties
     * @return
     */
    public static ReactiveRedisConnectionFactory getReactiveRedisConnectionFactory(RedisProperties redisProperties) {
        return getReactiveRedisConnectionFactory("default",redisProperties);
    }

    /**
     * 获取 {@link ReactiveRedisConnectionFactory}
     * @param name
     * @param redisProperties
     * @return
     */
    public static LettuceConnectionFactory getReactiveRedisConnectionFactory(String name , RedisProperties redisProperties) {
        /**
         * redis connection config
         */
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperties.getHost());
        configuration.setPort(redisProperties.getPort());
        configuration.setDatabase(redisProperties.getDatabase());
        Objects.requireNonNull(redisProperties.getJedis(),"redisProperties jedis config cannot be null!");
        Objects.requireNonNull(redisProperties.getJedis().getPool(),"redisProperties jedis config cannot be null!");
        RedisProperties.Pool pool = redisProperties.getJedis().getPool();
        /**
         * pool config
         */
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(pool.getMaxActive());
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMinIdle(pool.getMinIdle());
        poolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        LettucePoolingClientConfiguration clientConfig  = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .commandTimeout(redisProperties.getTimeout())
                .shutdownTimeout(Duration.ZERO)
                .build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration,clientConfig);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            log.info("Configure [{}] , {}  ",name,objectMapper.writeValueAsString(redisProperties));
        }catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
        return factory;
    }

}
