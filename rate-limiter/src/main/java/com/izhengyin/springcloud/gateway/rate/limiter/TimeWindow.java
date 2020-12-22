package com.izhengyin.springcloud.gateway.rate.limiter;

import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-27 14:57
 */
public enum TimeWindow {
    /**
     * 按秒限流
     */
    ONE_SECOND("1s",1, RateLimiterRedisScripts.getRateLimiterScript(1)),
    /**
     * 按5秒限流
     */
    FIVE_SECOND("5s",5, RateLimiterRedisScripts.getRateLimiterScript(5)),
    /**
     * 按10秒限流
     */
    TEN_SECOND("10s",10, RateLimiterRedisScripts.getRateLimiterScript(10)),
    /**
     * 按30秒限流
     */
    THIRTY_SECOND("30s",30, RateLimiterRedisScripts.getRateLimiterScript(30)),
    /**
     * 按分限流
     */
    ONE_MINUTE("1m",60, RateLimiterRedisScripts.getRateLimiterScript(60)),

    /**
     * 按5分限流
     */
    FIVE_MINUTE("5m",300, RateLimiterRedisScripts.getRateLimiterScript(300)),
    /**
     * 按10分限流
     */
    TEN_MINUTE("10m",600, RateLimiterRedisScripts.getRateLimiterScript(600)),
    /**
     * 按30分限流
     */
    THIRTY_MINUTE("30m",1800, RateLimiterRedisScripts.getRateLimiterScript(1800)),

    /**
     * 按时限流
     */
    ONE_HOUR("1h",3600, RateLimiterRedisScripts.getRateLimiterScript(3600));

    private String code;
    private Integer unitTime;
    private RedisScript<List<Long>> redisScript;

    TimeWindow(String code, Integer unitTime, RedisScript<List<Long>> redisScript) {
        this.code = code;
        this.unitTime = unitTime;
        this.redisScript = redisScript;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setUnitTime(Integer unitTime) {
        this.unitTime = unitTime;
    }

    public Integer getUnitTime() {
        return unitTime;
    }

    public RedisScript<List<Long>> getRedisScript() {
        return redisScript;
    }

    public void setRedisScript(RedisScript<List<Long>> redisScript) {
        this.redisScript = redisScript;
    }

    public static TimeWindow get(String code){
        for (TimeWindow timeWindow : TimeWindow.values()){
            if(timeWindow.getCode().equalsIgnoreCase(code)){
                return timeWindow;
            }
        }
        return null;
    }
}
