package com.izhengyin.springcloud.gateway.rate.limiter;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-12-22 16:44
 */
public interface BeanName {
    String RATE_LIMITER_PROPERTIES = "rateLimiterProperties";
    String RATE_LIMITER_CONFIG = "rateLimiterConfig";
    String RATE_LIMITER_CONFIG_UTILS = "rateLimiterConfigUtils";
    String RATE_LIMITER_METRICS = "rateLimiterMetrics";
    String RATE_LIMITER_FILTER = "rateLimiterFilter";
    String RATE_LIMITER_CONFIG_REFRESH = "rateLimiterConfigRefresh";

}