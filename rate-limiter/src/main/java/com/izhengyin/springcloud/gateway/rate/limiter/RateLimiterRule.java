package com.izhengyin.springcloud.gateway.rate.limiter;

import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-10-16 14:43
 */
@Data
@ToString
public class RateLimiterRule {
    /**
     * 配置名
     */
    private String name;
    /**
     * 原始串
     */
    private String source;
    /**
     * 令牌桶速率
     */
    private Integer rate;
    /**
     * 令牌桶容量
     */
    private Integer capacity;
    /**
     * 限流的时间窗口
     */
    private TimeWindow timeWindow;
    /**
     * 验证的类型 {@link AuthenticationType}
     */
    private AuthenticationType authenticationType;
    /**
     * 触发限流时返回的HTTP状态码 , default 403
     */
    private HttpStatus httpStatus = HttpStatus.FORBIDDEN;
}
