package com.izhengyin.springcloud.gateway.rate.limiter;
/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-29 10:28
 */
class RateLimiterException extends RuntimeException {
    RateLimiterException(String message){
        super(message);
    }
}
