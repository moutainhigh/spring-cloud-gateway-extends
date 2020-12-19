package com.izhengyin.springcloud.gateway.base.filters;

import com.alibaba.fastjson.JSON;
import com.izhengyin.springcloud.gateway.base.StatisticsService;
import com.izhengyin.springcloud.gateway.base.constant.Attribute;
import com.izhengyin.springcloud.gateway.base.pojo.Api;
import com.izhengyin.springcloud.gateway.base.pojo.Metrics;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * 网关指标采集 filter
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-22 14:44
 */
@Slf4j
@Component
public class MetricsGlobalFilter implements GlobalFilter, Ordered {
    @Value("${spring.application.name}")
    private String application;
    @Value("${spring.application.enable-access-log:true}")
    private Boolean enableAccessLog;
    private Counter requestCounter;
    private Histogram responseTimeHistogram;
    private final StatisticsService statisticsService;
    private static Logger gatewayAccessLog = LoggerFactory.getLogger("gatewayAccessLog");
    public MetricsGlobalFilter(CollectorRegistry registry , StatisticsService statisticsService){
        this.statisticsService = statisticsService;
        createPrometheusCollector(registry);
    }

    @Override
    public int getOrder() {
        return 10000;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .doOnSuccess(v -> this.createMetricsAndRecord(exchange))
                .doOnError(throwable -> this.createMetricsAndRecord(exchange,throwable));
    }
    /**
     * 创建并记录指标
     * @param swe
     */
    public void createMetricsAndRecord(ServerWebExchange swe){
        createMetricsAndRecord(swe,null);
    }
    /**
     * 创建并记录指标
     * @param swe
     * @param throwable
     */
    public void createMetricsAndRecord(ServerWebExchange swe , Throwable throwable){
        Api api = swe.getAttribute(Attribute.API);
        if(Objects.isNull(api)){
            return;
        }
        String clientIp = Optional.ofNullable(swe.getAttribute(Attribute.CLIENT_IP)).map(String::valueOf).orElse("");
        long requestTimeMs = (long) Optional.ofNullable(swe.getAttribute(Attribute.REQUEST_TIME_MS)).orElse(0L);
        ServerHttpRequest request = swe.getRequest();
        final Metrics metrics = new Metrics();
        metrics.setGateway(application);
        metrics.setService(api.getServer());
        metrics.setName(api.getName());
        metrics.setMapping(api.getMappingInfo());
        metrics.setMethod(api.getMethod());
        metrics.setReqMethod(request.getMethodValue());
        metrics.setCode(Optional.ofNullable(swe.getResponse().getStatusCode()).map(HttpStatus::value).orElse(0));
        metrics.setUri(request.getURI().getPath());
        metrics.setUserAgent(request.getHeaders().getFirst("User-Agent"));
        metrics.setClientIp(clientIp);
        metrics.setTimestamp(new Date(requestTimeMs));
        metrics.setErrorType(Optional.ofNullable(throwable).map(err -> err.getClass().getName()).orElse("nil"));
        metrics.setErrorMessage(Optional.ofNullable(throwable).map(Throwable::getMessage).orElse("nil"));
        metrics.setResTimeMs(System.currentTimeMillis() - metrics.getTimestamp().getTime());
        //记录 metrics
        record(metrics);
        //网关内部统计
        this.statisticsService.collect(metrics);
    }


    /**
     * 记录 Metrics
     * @param metrics
     */
    private void record(Metrics metrics){
        //日志记录
        if(enableAccessLog){
            gatewayAccessLog.info(JSON.toJSONString(metrics));
        }
        //prometheus 指标记录
        requestCounter.labels(
                metrics.getGateway(),
                metrics.getService(),
                metrics.getName(),
                metrics.getMapping(),
                metrics.getMethod(),
                metrics.getCode()+"",
                metrics.getErrorType()
        ).inc();
        responseTimeHistogram.labels(
                metrics.getGateway(),
                metrics.getService(),
                metrics.getName(),
                metrics.getMapping(),
                metrics.getMethod(),
                metrics.getCode()+"",
                metrics.getErrorType()
        ).observe(metrics.getResTimeMs().doubleValue());
    }

    /**
     * prometheus 收集器
     * @param registry
     */
    private void createPrometheusCollector(CollectorRegistry registry){
        requestCounter = Counter.build()
                .name("gateway_request_counter")
                .labelNames("application","service","name","mapping","method","code","errorType")
                .help("requests counter").register(registry);
        responseTimeHistogram = Histogram.build()
                .name("gateway_response_time")
                .labelNames("application","service","name","mapping","method","code","errorType")
                .help("response bytes histogram").register(registry);
    }

}