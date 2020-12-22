package com.izhengyin.springcloud.gateway.rate.limiter;
import com.alibaba.fastjson.JSON;
import com.izhengyin.springcloud.gateway.base.MetricsService;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-18 16:39
 */
@Configuration
@Slf4j
public class GatewayRateLimiterConfiguration{

    @ConfigurationProperties(
            prefix = "rate-limiter"
    )
    @Bean(BeanName.RATE_LIMITER_PROPERTIES)
    @RefreshScope
    public Properties rateLimiterProperties(){
        return new Properties();
    }

    @Bean(BeanName.RATE_LIMITER_CONFIG)
    @RefreshScope
    public ConcurrentHashMap<String,Config> rateLimiterConfig(@Qualifier(BeanName.RATE_LIMITER_PROPERTIES) Properties rateLimiterProperties){
        Objects.requireNonNull(rateLimiterProperties,"rateLimiterProperties cannot be null!");
        ConcurrentHashMap<String,Config> configs = new ConcurrentHashMap<>(rateLimiterProperties.size());
        rateLimiterProperties.forEach((k,v) -> {
            String name = String.valueOf(k).toLowerCase();
            String value = String.valueOf(v);
            try {
                configs.put(name,Config.create(name,value));
            }catch (RuntimeException e){
                log.error("rateLimiterConfigs {} -> {} , error message  {}",name,value,e.getMessage(),e);
            }
        });
        log.info("rateLimiterConfig {}", JSON.toJSONString(configs));
        return configs;
    }

    @Bean(BeanName.RATE_LIMITER_CONFIG_UTILS)
    @RefreshScope
    public RateLimiterConfigUtils rateLimiterConfigUtils(@Qualifier(BeanName.RATE_LIMITER_CONFIG) ConcurrentHashMap<String,Config> configs){
        return new RateLimiterConfigUtils(configs);
    }

    @Bean(BeanName.RATE_LIMITER_METRICS)
    public RateLimiterMetrics rateLimiterMetrics(CollectorRegistry registry , @Value("${gateway.rate-limiter-metrics-name:gateway_request_rate_limiter_counter}") String metricsName){
        return new RateLimiterMetrics(registry,metricsName);
    }

    @Bean(BeanName.RATE_LIMITER_FILTER)
    public RateLimiterFilter rateLimiterFilter(
            ReactiveStringRedisTemplate redisTemplate ,
            @Qualifier(BeanName.RATE_LIMITER_CONFIG_UTILS) RateLimiterConfigUtils configUtils,
            MetricsService metricsService
    ){
        return new RateLimiterFilter(redisTemplate,configUtils,metricsService);
    }

    @Bean(BeanName.RATE_LIMITER_CONFIG_REFRESH)
    public RateLimiterConfigRefresh rateLimiterConfigRefresh(org.springframework.cloud.context.scope.refresh.RefreshScope refreshScope){
        return new RateLimiterConfigRefresh(refreshScope);
    }
}