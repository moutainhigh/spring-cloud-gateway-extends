package com.izhengyin.springcloud.gateway.base.config;

import com.izhengyin.springcloud.gateway.base.ApiManageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-12-19 14:38
 */
@EnableScheduling
@Configuration
public class GatewayScheduleConfiguration {
    private final ApiManageService apiManageService;
    public GatewayScheduleConfiguration(ApiManageService apiManageService){
        this.apiManageService = apiManageService;
    }

    /**
     * 排序接口
     */
    @Scheduled(cron = "1 */1 * * * ?")
    public void resortApisCron(){
        apiManageService.resortApis();
    }
}
