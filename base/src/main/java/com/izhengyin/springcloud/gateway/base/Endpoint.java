package com.izhengyin.springcloud.gateway.base;

import com.izhengyin.springcloud.gateway.base.pojo.Api;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 09:44
 */
@RestController
@RequestMapping("/actuator/gateway")
public class Endpoint {

    private final ApiManageService apiManageService;
    private final StatisticsService statisticsService;
    public Endpoint(ApiManageService apiManageService, StatisticsService statisticsService){
        this.apiManageService = apiManageService;
        this.statisticsService = statisticsService;
    }

    /**
     * 刷新全部服务接口
     * @return
     */
    @PostMapping("/services/refresh")
    public Mono<List<Map<String, Api>>> refreshServices(){
        return apiManageService.reloadAllApis()
                .doOnNext(v -> statisticsService.refreshCounterWindows());
    }

    /**
     * 刷新特点服务接口
     * @param service
     * @return
     */
    @PostMapping("/service/{service}/refresh")
    public Mono<List<Map<String,Api>>> refreshService(@PathVariable String service){
        return apiManageService.refreshApis(service)
                .doOnNext(v -> statisticsService.refreshCounterWindows());
    }

    /**
     * 获取所有网关管理的接口
     * @return
     */
    @GetMapping("/apis")
    public List<Api> apis(){
        return apiManageService.getApis();
    }

    /**
     * 获取网关当前统计窗口的指标
     * @return
     */
    @GetMapping("/metrics")
    public Mono<List<String>> metrics(){
        return statisticsService.getAndClearCounters();
    }
}
