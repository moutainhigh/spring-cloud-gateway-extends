package com.izhengyin.springcloud.gateway.webagent;
/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 14:16
 */
public class GatewayWebApiDefine {
    private String name;
    private String method;
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

    public String getMappingInfo() {
        return mappingInfo;
    }

    public void setMappingInfo(String mappingInfo) {
        this.mappingInfo = mappingInfo;
    }
}