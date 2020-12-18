package com.izhengyin.springcloud.gateway.webagent;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 13:42
 */
@RestController
public class GatewayWebAgentEndpointController {
    private final GatewayWebAgentService gatewayWebAgentService;
    GatewayWebAgentEndpointController(GatewayWebAgentService gatewayWebAgentService) {
        this.gatewayWebAgentService = gatewayWebAgentService;
    }

    /**
     * 暴露服务接口定义
     * @return
     */
    @GetMapping("/actuator/gateway/server/apis")
    public List<GatewayWebApiDefine> getApis(){
        return gatewayWebAgentService.getApiDefines();
    }
}