package com.izhengyin.springcloud.gateway.base.filters;

import com.izhengyin.springcloud.gateway.base.MetricsService;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关指标采集 filter
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-22 14:44
 */
@Slf4j
@Component
public class MetricsGlobalFilter implements GlobalFilter, Ordered {
    private final MetricsService metricsService;
    public MetricsGlobalFilter(CollectorRegistry registry , MetricsService metricsService){
        this.metricsService = metricsService;
    }

    @Override
    public int getOrder() {
        return 10000;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .doOnSuccess(v -> this.metricsService.createMetricsAndRecord(exchange))
                .doOnError(throwable -> this.metricsService.createMetricsAndRecord(exchange,throwable));
    }


}