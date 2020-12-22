package com.izhengyin.springcloud.gateway.rate.limiter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import java.util.Objects;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-28 17:38
 */
public class RateLimiterMetrics {
    private static Counter COUNTER;
    public RateLimiterMetrics(CollectorRegistry registry,String gatewayRateLimiterMetricsName){
        COUNTER = Counter.build()
                .name(gatewayRateLimiterMetricsName)
                .labelNames("name","timeWindow","autType")
                .help("rate limiter counter").register(registry);
    }
    /**
     * 记录限流指标
     * @param rule {@link RateLimiterRule}
     */
    static void counter(RateLimiterRule rule){
        if(Objects.nonNull(rule)){
            COUNTER.labels(rule.getName(),rule.getTimeWindow().getCode(),rule.getAuthenticationType().getType());
        }

    }
}
