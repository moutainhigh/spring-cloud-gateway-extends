package com.izhengyin.springcloud.gateway.base.config;
import com.izhengyin.springcloud.gateway.base.config.factory.RedisConnectionFactory;
import com.izhengyin.springcloud.gateway.base.constant.BeanName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * 网关Redis配置
 * @author zhengyin  zhengyinit@outlook.com
 * @date Created on 2019/3/19 10:14 AM
 */
@Configuration
@Slf4j
public class GatewayRedisConfiguration {

    @Primary
    @Bean(name = BeanName.GATEWAY_REDIS_TEMPLATE)
    public StringRedisTemplate stringRedisTemplate(@Qualifier("gatewayRedisConnectionFactory") LettuceConnectionFactory lettuceConnectionFactory){
        return new StringRedisTemplate(lettuceConnectionFactory);
    }

    @Primary
    @Bean(name = BeanName.GATEWAY_REACTIVE_REDIS_TEMPLATE)
    public ReactiveStringRedisTemplate reactiveRedisTemplateString (@Qualifier("gatewayRedisConnectionFactory") LettuceConnectionFactory lettuceConnectionFactory) {
        return new ReactiveStringRedisTemplate(lettuceConnectionFactory, RedisSerializationContext.string());
    }

    @Primary
    @Bean("gatewayRedisConnectionFactory")
    public LettuceConnectionFactory reactiveRedisConnectionFactory(@Qualifier(BeanName.GATEWAY_REDIS_PROPERTIES) RedisProperties redisProperties) {
        return RedisConnectionFactory.getReactiveRedisConnectionFactory("gatewayRedisConnectionFactory",redisProperties);
    }
}