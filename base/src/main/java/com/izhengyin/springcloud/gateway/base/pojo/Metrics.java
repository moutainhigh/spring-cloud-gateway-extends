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
    /**
     * 网关名
     */
    private String gateway;
    /**
     * 服务名
     */
    private String service;
    /**
     * 自定义接口名（通过 spring @XXXMapping(name = xxx) 指定 ）
     */
    private String name;
    /**
     * spring 接口描述的mapping
     */
    private String mapping;
    /**
     * 处理接口请求的 Java method
     */
    private String method;
    /**
     * 请求的 http method
     */
    private String httpMethod;
    /**
     * 请求 uri
     */
    private String uri;
    /**
     * 响应状态码
     */
    private Integer httpCode;
    /**
     * 请求响应时间
     */
    private Long resTimeMs;
    /**
     * UA
     */
    private String userAgent;
    /**
     * 请求来源IP
     */
    private String clientIp;
    /**
     * 当发送错误时的类别
     */
    private String errorType;
    /**
     * 当发送错误时的描述
     */
    private String errorMessage;
    /**
     * 请求时间
     */
    private Date timestamp;
}