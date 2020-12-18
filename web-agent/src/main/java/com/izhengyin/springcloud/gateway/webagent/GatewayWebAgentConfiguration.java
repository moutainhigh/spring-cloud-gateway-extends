package com.izhengyin.springcloud.gateway.webagent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 13:41
 */
@Configuration
public class GatewayWebAgentConfiguration {
    @Bean
    public GatewayWebAgentService gatewayWebAgentService(){
        return new GatewayWebAgentService();
    }
    @Bean
    public GatewayWebAgentEndpointController agentEndpointController(GatewayWebAgentService gatewayWebAgentService){
        return new GatewayWebAgentEndpointController(gatewayWebAgentService);
    }
}
