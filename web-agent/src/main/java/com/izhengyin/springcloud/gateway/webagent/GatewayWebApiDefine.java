package com.izhengyin.springcloud.gateway.webagent;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 14:16
 */
public class GatewayWebApiDefine {
    private String name;
    private String method;
    private Set<RequestMethod> httpMethod;
    private Set<String> pathPatterns;
    private String mappingInfo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Set<RequestMethod> getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(Set<RequestMethod> httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Set<String> getPathPatterns() {
        return pathPatterns;
    }

    public void setPathPatterns(Set<String> pathPatterns) {
        this.pathPatterns = pathPatterns;
    }

    public String getMappingInfo() {
        return mappingInfo;
    }

    public void setMappingInfo(String mappingInfo) {
        this.mappingInfo = mappingInfo;
    }
}