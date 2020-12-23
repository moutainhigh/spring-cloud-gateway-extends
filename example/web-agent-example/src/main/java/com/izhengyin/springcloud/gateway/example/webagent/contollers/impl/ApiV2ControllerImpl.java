package com.izhengyin.springcloud.gateway.example.webagent.contollers.impl;

import com.izhengyin.springcloud.gateway.example.webagent.contollers.ApiController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-12-18 16:00
 */
@RestController
@RequestMapping("/api/v2")
public class ApiV2ControllerImpl implements ApiController {

    @Override
    public String hello(String name){
        return "你好 "+name;
    }

    @Override
    public String getResourceById(@PathVariable int id){
        throw new UnsupportedOperationException("接口取消了");
    }
}