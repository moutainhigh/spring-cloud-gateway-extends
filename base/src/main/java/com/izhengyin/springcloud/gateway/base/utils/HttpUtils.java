package com.izhengyin.springcloud.gateway.base.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-02-21 16:10
 */
@Slf4j
public class HttpUtils {
    public final static String LOCAL_IP = "127.0.0.1";
    public final static String UNKNOWN = "unknown";
    public final static String IP_SPLIT_MARK = ",";

    /**
     * 获取客户端IP
     * @param request {@link ServerHttpRequest}
     * return   客户端IP
     */
    public static String getClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && ip.length() != 0 && ! UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(IP_SPLIT_MARK)) {
                ip = ip.split(IP_SPLIT_MARK)[0];
            }
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = Optional.ofNullable(request.getRemoteAddress()).map(v -> v.getAddress().getHostAddress()).orElse(UNKNOWN);
        }
        return ip;
    }

    /**
     * 获取客户端agent
     * @param request
     * @return
     */
    public static String getUserAgent(ServerHttpRequest request){
        return request.getHeaders().getFirst("User-Agent");
    }

}
