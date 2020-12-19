package com.izhengyin.springcloud.gateway.webagent;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 13:58
 */

public class GatewayWebAgentService implements ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * 获取服务的API定义描述
     * @return
     */
    List<GatewayWebApiDefine> getApiDefines(){
        RequestMappingHandlerMapping requestMappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);
        List<GatewayWebApiDefine> gatewayWebApiDefines = new ArrayList<>();
        requestMappingHandlerMapping.getHandlerMethods()
                .forEach((requestMappingInfo, handlerMethod) -> {
                    GatewayWebApiDefine gatewayWebApiDefine = new GatewayWebApiDefine();
                    gatewayWebApiDefine.setName(Optional.ofNullable(requestMappingInfo.getName()).orElse(""));
                    gatewayWebApiDefine.setMethod(handlerMethod.toString());
                    gatewayWebApiDefine.setHttpMethod(requestMappingInfo.getMethodsCondition().getMethods());
                    gatewayWebApiDefine.setPathPatterns(requestMappingInfo.getPatternsCondition().getPatterns());
                    gatewayWebApiDefine.setMappingInfo(requestMappingInfo.toString());
                    gatewayWebApiDefines.add(gatewayWebApiDefine);
                });
        return gatewayWebApiDefines;
    }

}
