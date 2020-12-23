package com.izhengyin.springcloud.gateway.base;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.izhengyin.springcloud.gateway.base.pojo.Api;
import com.izhengyin.springcloud.gateway.base.pojo.Metrics;
import com.izhengyin.springcloud.gateway.base.utils.UnixTimeStampUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-29 12:19
 */
@Service
@Slf4j
@DependsOn(value = "apiManageService")
public class StatisticsService {
    private final static int COUNTER_WINDOW_MOD = 2;
    private final static int CAS_MAX_LOOP = 10;
    private final AtomicInteger minuteOffset = new AtomicInteger();
    private final String application;
    private final String collectKey;
    private final StringRedisTemplate redisTemplate;
    private final ApiManageService apiManageService;
    private final List<Map<String,List<AtomicInteger>>> counterWindows = new ArrayList<>();
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1,
            1,
            1000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(10),
            new ThreadFactoryBuilder()
                    .setNameFormat("statistics-collect")
                    .build(),
            new ThreadPoolExecutor.AbortPolicy()
    );
    public StatisticsService(
            @Value("${spring.application.name}") String application ,
            ApiManageService apiManageService,
            StringRedisTemplate redisTemplate) {
        this.apiManageService = apiManageService;
        this.redisTemplate = redisTemplate;
        this.application = application;
        this.collectKey = "metrics-collect-"+this.application;
        this.initCounterWindows();
    }

    /**
     * 记录接口请求数
     * @param metrics
     */
    public void collect(Metrics metrics){
        boolean isError = Optional.ofNullable(metrics.getHttpCode())
                            .map(code -> code >= HttpStatus.BAD_REQUEST.value() && code != HttpStatus.NOT_FOUND.value())
                            .orElse(false);
        int resTime = Optional.ofNullable(metrics.getResTimeMs()).map(Long::intValue).orElse(0);
        collect(metrics.getName(),resTime,isError);
    }

    /**
     * 获取收集的指标
     * @return
     */
    Mono<List<String>> getAndClearCounters(){
        return Mono.just(new ArrayList<String>())
                .subscribeOn(Schedulers.single())
                .map(counters -> {
                    while (true){
                        String value = redisTemplate.opsForList().rightPop(collectKey);
                        if(StringUtils.isEmpty(value)){
                            break;
                        }
                        counters.add(value);
                    }
                    return counters;
                });
    }

    /**
     *  重新初始化  counterWindows
     */
    void refreshCounterWindows(){
        if(!counterWindows.isEmpty() && counterWindows.size() == COUNTER_WINDOW_MOD){
            counterWindows.set(0,createApiCounters());
            counterWindows.set(1,createApiCounters());
            log.info("refreshCounterWindows {} ", JSON.toJSONString(counterWindows, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
        }
    }

    /**
     * 记录接口请求数
     * @param name 接口名
     * @param isError 是否有错误
     */
    private void collect(String name , int resTime , boolean isError){
        if(counterWindows.size() != COUNTER_WINDOW_MOD){
            return;
        }
        int minute = (int) UnixTimeStampUtils.getMinute();
        List<AtomicInteger> counters = counterWindows.get(minute % COUNTER_WINDOW_MOD).get(name);
        if(Objects.isNull(counters) || counters.size() != 4){
            return;
        }
        //request num counter
        counters.get(0).getAndIncrement();
        //error counter
        if(isError){
            counters.get(1).getAndIncrement();
        }
        //response time counter
        AtomicInteger resTimeCounter = counters.get(2);
        int maxLoop = CAS_MAX_LOOP;
        int old = resTimeCounter.get();
        while (!resTimeCounter.compareAndSet(old , old + resTime)){
            old = resTimeCounter.get();
            maxLoop --;
            if(maxLoop <= 0){
                break;
            }
        }
        if(log.isDebugEnabled()){
            log.debug("collect {} , {} , {} , {} , {} ",name,counters.get(0).get(),counters.get(1).get(),counters.get(2).get(),counters.get(3).get());
        }
        //时间窗口变化，收集指标
        int offset = minuteOffset.get();
        if(offset != minute && minuteOffset.compareAndSet(offset,minute)){
            Map<String,List<AtomicInteger>> counterWindow = counterWindows.get((minute - 1) % 2);
            storeMetrics(counters,counterWindow);
        }
    }

    /**
     * 存储 metrics
     * @param counters
     * @param counterWindow
     */
    private void storeMetrics(List<AtomicInteger> counters , Map<String,List<AtomicInteger>> counterWindow){
        List<String> metrics = new ArrayList<>(counters.size());
        counterWindow.forEach((k,cs) -> {
            int num = cs.get(0).getAndSet(0);
            int errNum = cs.get(1).getAndSet(0);
            int resTimeTotal = cs.get(2).getAndSet(0);
            if(num > 0){
                metrics.add(k+","+num+","+errNum+","+resTimeTotal);
            }
        });
        try {
            if(!metrics.isEmpty()){
                executor.execute(() -> {
                    //指标存储到Redis
                    Long l = redisTemplate.opsForList().leftPush(collectKey, JSON.toJSONString(metrics));
                    log.info("Collect Metrics {} , query size {} ", metrics.size(), l);
                });
            }
        }catch (RejectedExecutionException e){
            log.error("collect {}",e.getMessage(),e);
        }
    }

    /**
     * 首次初始化 counterWindows
     */
    private void initCounterWindows(){
        counterWindows.add(0,createApiCounters());
        counterWindows.add(1,createApiCounters());
        log.info("initCounterWindows {} ", JSON.toJSONString(counterWindows, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
    }

    /**
     * 创建ApiCounters
     * @return
     */
    private Map<String,List<AtomicInteger>> createApiCounters(){
        List<Api> apis = apiManageService.getApis();
        Map<String,List<AtomicInteger>> map = new HashMap<>(apis.size());
        apis.forEach(api -> map.put(api.getMethod(), Arrays.asList(new AtomicInteger(0),new AtomicInteger(0),new AtomicInteger(0))));
        return map;
    }

}
