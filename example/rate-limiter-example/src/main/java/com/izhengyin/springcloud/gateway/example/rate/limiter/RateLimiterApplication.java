package com.izhengyin.springcloud.gateway.example.rate.limiter;
import com.izhengyin.springcloud.gateway.base.Scan;
import com.izhengyin.springcloud.gateway.base.constant.BeanName;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-12-19 12:00
 */
@SpringBootApplication(scanBasePackageClasses = {RateLimiterApplication.class, Scan.class})
public class RateLimiterApplication {
    public static void main(String[] args) {
        SpringApplication.run(RateLimiterApplication.class,args);
    }

    @org.springframework.context.annotation.Configuration
    public static class Configuration{
        @ConfigurationProperties(prefix = "redis.gateway.test")
        @Bean(BeanName.GATEWAY_REDIS_PROPERTIES)
        @Primary
        public RedisProperties redisProperties(){
            return new RedisProperties();
        }
    }
}
