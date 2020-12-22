package com.izhengyin.springcloud.gateway.rate.limiter;

import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-27 13:52
 */
@Data
@ToString
public class Config {
    /**
     * 接口名
     */
    private String name;

    /**
     * 规则
     */
    private List<RateLimiterRule> rules;


    /**
     * 通过字符串的配置创建配置对象
     * @param name
     * @param values
     * @return
     */
    public static Config create(String name , String values){
        Config config = new Config();
        config.setName(name);
        if(StringUtils.isEmpty(values)){
            throw new IllegalArgumentException("config values is illegal！");
        }
        //设置规则
        config.setRules(Stream.of(values.split("\\|"))
                .map(String::trim)
                .distinct()
                .map(value -> {
                    RateLimiterRule rule = new RateLimiterRule();
                    rule.setName(name);
                    rule.setSource(value);
                    String[] arr = value.split(",");
                    if(arr.length < 2){
                        throw new IllegalArgumentException("config value is illegal！");
                    }
                    rule.setRate(Optional.of(arr[0]).map(String::trim).map(Integer::parseInt).orElse(-1));
                    rule.setCapacity(Optional.of(arr[1]).map(String::trim).map(Integer::parseInt).orElse(-1));
                    if(arr.length >= 3){
                        rule.setTimeWindow(Optional.of(arr[2]).map(String::trim).map(TimeWindow::get).orElseGet(() -> {
                            throw new IllegalArgumentException("timeWindow config value is illegal!");
                        }));
                    }
                    if(arr.length >= 4){
                        rule.setAuthenticationType(Optional.of(arr[3]).map(String::trim).map(AuthenticationType::get).orElseGet(() -> {
                            throw new IllegalArgumentException("AuthenticationType config value is illegal!");
                        }));
                    }
                    if(arr.length >= 5){
                        rule.setHttpStatus(Optional.of(arr[4]).map(String::trim).map(Integer::parseInt).map(HttpStatus::resolve).orElse(HttpStatus.FORBIDDEN));
                    }
                    return rule;
                })
                .collect(Collectors.toList())
        );

        return config;
    }
}
