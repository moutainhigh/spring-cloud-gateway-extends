package com.izhengyin.springcloud.gateway.base.pojo;

import lombok.Data;
import lombok.ToString;
import java.util.Date;

/**
 * @author zhengyin  <zhengyin.name@gmail.com>
 * @date Created on 2019/1/9 12:15 PM
 */
@Data
@ToString
public class Metrics {
    private String gateway;
    private String service;
    private String name;
    private String mapping;
    private String method;
    private String reqMethod;
    private String uri;
    private Integer code;
    private Long resTimeMs;
    private String userAgent;
    private String clientIp;
    private String errorType;
    private String errorMessage;
    private Date timestamp;
}
