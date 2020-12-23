package com.izhengyin.springcloud.gateway.base.pojo;
import lombok.Data;
import lombok.ToString;

import java.util.Set;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 11:52
 */
@Data
@ToString
public class Api {
    /**
     * 服务名
     */
    private String server;
    /**
     * 接口名
     */
    private String name;
    /**
     * 处理接口请求的method
     */
    private String method;
    /**
     * http method (可以多个)
     */
    private Set<String> httpMethod;
    /**
     * 路径规则 (可以多个)
     */
    private Set<String> pathPatterns;
    /**
     * spring mapping 描述
     */
    private String mappingInfo;
    /**
     * 请求数量（粗略值，用于近似排序）
     */
    private int requestNum;
}
