package com.izhengyin.springcloud.gateway.rate.limiter;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-27 14:04
 */
public enum AuthenticationType {
    /**
     * Ip验证
     */
    IP("ip"),
    /**
     * Ip - a段验证
     */
    IP_A("ip_a"),
    /**
     * Ip - b段验证
     */
    IP_B("ip_b"),
    /**
     * Ip - c段验证
     */
    IP_C("ip_c"),
    /**
     * 爬虫
     */
    SPIDER("spider");
    private String type;
    AuthenticationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static AuthenticationType get(String type){
        for (AuthenticationType authenticationType : AuthenticationType.values()){
            if(authenticationType.getType().equalsIgnoreCase(type)){
                return authenticationType;
            }
        }
        return null;
    }
}
