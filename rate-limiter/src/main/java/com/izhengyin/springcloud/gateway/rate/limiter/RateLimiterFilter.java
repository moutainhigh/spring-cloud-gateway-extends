package com.izhengyin.springcloud.gateway.rate.limiter;

import com.alibaba.fastjson.JSON;
import com.izhengyin.springcloud.gateway.base.MetricsService;
import com.izhengyin.springcloud.gateway.base.constant.Attribute;
import com.izhengyin.springcloud.gateway.base.pojo.Api;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-18 16:47
 */
@Slf4j
public class RateLimiterFilter implements GlobalFilter, Ordered {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimiterConfigUtils configUtils;
    private final MetricsService metricsService;
    RateLimiterFilter(
            ReactiveStringRedisTemplate redisTemplate,
            RateLimiterConfigUtils configUtils,
            MetricsService metricsService
    ){
        this.redisTemplate = redisTemplate;
        this.configUtils = configUtils;
        this.metricsService = metricsService;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Api api = exchange.getAttribute(Attribute.API);
        if(Objects.isNull(api)){
            return chain.filter(exchange);
        }
        //查找限流配置
        List<RateLimiterRule> matchedRateLimiterRules = configUtils.findRateLimiterRules(api.getMethod());
        if(matchedRateLimiterRules.isEmpty()){
            return chain.filter(exchange);
        }
        if(log.isDebugEnabled()){
            log.debug("matchedRateLimiterRules "+matchedRateLimiterRules.stream().map(RateLimiterRule::getSource).collect(Collectors.joining(" | ")));
        }
        //执行多个限流配置
        return Flux.fromIterable(matchedRateLimiterRules)
                .flatMap(rule -> rateLimiter(rule,exchange))
                .reduce(new ArrayList<RateLimiterResult>(),(list, result) -> {
                    list.add(result);
                    return list;
                })
                .flatMap(list -> {
                    //只要有一个配置触发限流，就直接返回
                    for (RateLimiterResult result : list){
                        if(!result.isAllowed()){
                            return setLimitedResponse(result.getRule(),exchange);
                        }
                    }
                    return chain.filter(exchange);
                });
    }

    /**
     * 令牌桶限流
     * @param rule
     * @param exchange
     * @return
     */
    private Mono<RateLimiterResult> rateLimiter(RateLimiterRule rule, ServerWebExchange exchange){
        final String limitKey = RateLimiterUtils.getLimitScriptKey(rule, exchange);
        if(StringUtils.isEmpty(limitKey)){
            if(log.isDebugEnabled()){
                log.debug("rateLimiter limitKey empty {} , {}",rule.getName(),rule.getSource());
            }
            return Mono.just(new RateLimiterResult(rule,true,-1L));
        }
        final List<String> keys = Arrays.asList(limitKey+"#tokenKey", limitKey+"#timestampKey");
        final List<String> args = RateLimiterUtils.getLimitScriptArgs(rule);


        //@see META-INF/scripts/request_rate_limiter.lua
        return this.redisTemplate.execute(rule.getTimeWindow().getRedisScript(), keys, args)
                //返回结果检查
                .filter(results -> results.size() == 2)
                //空值默认
                .switchIfEmpty(Flux.just(Arrays.asList(1L, -1L)))
                //脚本执行错误处理
                .onErrorResume(throwable -> {
                    log.error("rateLimiter {} execute script [{}] error {} , {} , {} ",rule.getSource(),rule.getTimeWindow().getRedisScript().getSha1(),throwable.getMessage(),keys,args,throwable);
                    return Flux.just(Arrays.asList(1L, -1L));
                })
                //将多个结果进行reduce计算
                .reduce(new ArrayList<Long>(), (longs, l) -> {
                    longs.addAll(l);
                    return longs;
                })
                //debug log
                .doOnNext(results -> {
                    if(Objects.nonNull(results) && results.size() == 2){
                        if(log.isDebugEnabled()){
                            log.debug("call rateLimiter script , rule "+rule.getSource()+" isAllow ["+(results.get(0) == 1L)+"] , residue token["+results.get(1)+"] , keys "+JSON.toJSONString(keys)+" , args "+JSON.toJSONString(args));
                        }
                        if(log.isInfoEnabled() && (results.get(0) != 1L)){
                            log.info("request limited , limitKey {} ",limitKey);
                        }
                    }
                })
                .map(results -> new RateLimiterResult(rule,results.get(0) == 1L,results.get(1)));
    }

    /**
     *返回访问受限的 response
     * @param rule
     * @param exchange
     * @return
     */
    private Mono<Void> setLimitedResponse(RateLimiterRule rule, ServerWebExchange exchange){
        ServerHttpResponse response = exchange.getResponse();
        if(response.isCommitted()){
            return Mono.empty();
        }
        response.setStatusCode(rule.getHttpStatus());
        response.getHeaders().add("x-request-limited","true");
        //指标
        RateLimiterMetrics.counter(rule);
        return response.setComplete()
                .doOnSuccess(v -> this.metricsService.createMetricsAndRecord(exchange,new RateLimiterException(rule.getSource())))
                .doOnError(throwable -> this.metricsService.createMetricsAndRecord(exchange,throwable));
    }

    /**
     * 限流结果
     */
    @Data
    @ToString
    @AllArgsConstructor
    private static class RateLimiterResult {
        private RateLimiterRule rule;
        private boolean allowed;
        private long residue;
    }

}
