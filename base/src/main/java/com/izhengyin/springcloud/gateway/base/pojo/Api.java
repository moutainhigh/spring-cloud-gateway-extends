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
    private String server;
    private String name;
    private String method;
    private Set<String> httpMethod;
    private Set<String> pathPatterns;
    private String mappingInfo;
    private int requestNum;
}
