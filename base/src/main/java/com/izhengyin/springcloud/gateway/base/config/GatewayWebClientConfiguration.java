package com.izhengyin.springcloud.gateway.base.config;

import com.izhengyin.springcloud.gateway.base.utils.WebClientBuildUtils;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 11:11
 */
@Configuration
public class GatewayWebClientConfiguration {

    @Bean
    @Primary
    @LoadBalanced
    public WebClient.Builder loadBalancedWebBuilder() {
        return WebClientBuildUtils.withTimeout(
                WebClient.builder().defaultHeader("Content-Type","application/json;charset:utf-8"),
                1000,
                3000,
                3000
        )
        .defaultHeader("Content-type","application/json;charset=utf-8");
    }

    @Bean
    @Primary
    public WebClient loadBalancedWebClient(WebClient.Builder builder){
        return builder.build();
    }

    @Bean("nonLBWebClient")
    public WebClient webClient() {
        return WebClientBuildUtils.withTimeout(
                WebClient.builder().defaultHeader("Content-Type","application/json;charset:utf-8"),
                1000,
                3000,
                3000
        )
        .defaultHeader("Content-type","application/json;charset=utf-8")
        .build();
    }

}
