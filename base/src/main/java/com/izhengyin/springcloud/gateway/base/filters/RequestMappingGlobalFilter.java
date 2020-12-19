package com.izhengyin.springcloud.gateway.base.filters;

import com.izhengyin.springcloud.gateway.base.ApiManageService;
import com.izhengyin.springcloud.gateway.base.constant.Attribute;
import com.izhengyin.springcloud.gateway.base.pojo.Api;
import com.izhengyin.springcloud.gateway.base.utils.HttpUtils;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 请求映射到API对象
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-21 11:55
 */
@Slf4j
@Component
public class RequestMappingGlobalFilter implements GlobalFilter, Ordered {
    @Value("${spring.application.name}")
    private String application;
    private final ApiManageService apiManageService;
    private final GatewayProperties gatewayProperties;
    private final int order;
    private final PathMatcher pathMatcher;
    private final static Api EMPTY_API;
    static {
        EMPTY_API = new Api();
        EMPTY_API.setServer("nil");
        EMPTY_API.setName("nil");
        EMPTY_API.setMappingInfo("nil");
        EMPTY_API.setHttpMethod(new HashSet<>());
        EMPTY_API.setPathPatterns(new HashSet<>());
    }
    public RequestMappingGlobalFilter(
            ApiManageService apiManageService,
            CollectorRegistry registry,
            GatewayProperties gatewayProperties
    ){
        this.apiManageService = apiManageService;
        this.gatewayProperties = gatewayProperties;
        //避开路由单独定义的 filter ，让默认的Filter先执行
        this.order = Optional.ofNullable(this.gatewayProperties.getDefaultFilters()).map(List::size).orElse(0) + 10;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if(Objects.isNull(route)){
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        //请求的IP
        exchange.getAttributes().put(Attribute.CLIENT_IP, HttpUtils.getClientIp(request));
        //收到请求的时间
        exchange.getAttributes().put(Attribute.REQUEST_TIME_MS,System.currentTimeMillis());
        Api api = matchApi(route.getId(),request.getMethodValue().toUpperCase(),request.getPath().value());
        if(Objects.nonNull(api)){
            //警告：线程不安全（近似值，用于排序热点接口，不需要精确）@zhengyin 2020-09-21
            api.setRequestNum(api.getRequestNum() + 1);
            //请求的接口
            exchange.getAttributes().put(Attribute.API,api);
        }
        return chain.filter(exchange);
    }


    /**
     * 匹配请求的API
     * @param server
     * @param requestMethod
     * @param uri
     * @return {@link Api} 匹配不到返回空API
     */
    private Api matchApi(String server , String requestMethod , String uri){
        return apiManageService.getApis(server)
                .stream()
                .filter(v -> {
                    //HttpMethod 没有代表所有的方法
                    if(!v.getHttpMethod().isEmpty() && !v.getHttpMethod().contains(requestMethod)){
                        return false;
                    }
                    //Path Patterns 必须匹配
                    if(v.getPathPatterns().isEmpty()){
                        return false;
                    }
                    //匹配路径
                    for (String pattern : v.getPathPatterns()){
                        if (this.pathMatcher.match(pattern,uri)){
                            return true;
                        }
                    }
                    return false;
                })
                .findFirst()
                .orElse(EMPTY_API);
    }
}

