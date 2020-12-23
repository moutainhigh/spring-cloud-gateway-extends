package com.izhengyin.springcloud.gateway.base;

import com.alibaba.fastjson.JSON;
import com.izhengyin.springcloud.gateway.base.constant.Attribute;
import com.izhengyin.springcloud.gateway.base.pojo.Api;
import com.izhengyin.springcloud.gateway.base.pojo.Metrics;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-12-22 16:07
 */
@Service
public class MetricsService {

    @Value("${spring.application.name}")
    private String application;
    @Value("${gateway.enable-access-log:true}")
    private Boolean enableAccessLog;
    private Counter requestCounter;
    private Histogram responseTimeHistogram;
    private final StatisticsService statisticsService;
    private static Logger gatewayAccessLog = LoggerFactory.getLogger("gatewayAccessLog");
    public MetricsService(
            CollectorRegistry registry ,
            StatisticsService statisticsService,
            @Value("${gateway.request-counter-metrics-name:gateway_request_counter}") String gatewayRequestCounterMetricsName,
            @Value("${gateway.response-time-metrics-name:gateway_response_time}") String gatewayResponseTimeMetricsName
    ){
        this.statisticsService = statisticsService;
        createPrometheusCollector(registry,gatewayRequestCounterMetricsName,gatewayResponseTimeMetricsName);
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
        metrics.setHttpMethod(request.getMethodValue());
        metrics.setHttpCode(Optional.ofNullable(swe.getResponse().getStatusCode()).map(HttpStatus::value).orElse(0));
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
                metrics.getHttpCode()+"",
                metrics.getErrorType()
        ).inc();
        responseTimeHistogram.labels(
                metrics.getGateway(),
                metrics.getService(),
                metrics.getName(),
                metrics.getMapping(),
                metrics.getMethod(),
                metrics.getHttpCode()+"",
                metrics.getErrorType()
        ).observe(metrics.getResTimeMs().doubleValue());
    }

    /**
     * prometheus 收集器
     * @param registry
     */
    private void createPrometheusCollector(CollectorRegistry registry,String counterMetricsName,String responseTimeMetricsName){
        requestCounter = Counter.build()
                .name(counterMetricsName)
                .labelNames("application","service","name","mapping","method","code","errorType")
                .help("requests counter").register(registry);
        responseTimeHistogram = Histogram.build()
                .name(responseTimeMetricsName)
                .labelNames("application","service","name","mapping","method","code","errorType")
                .help("response bytes histogram").register(registry);
    }

}
