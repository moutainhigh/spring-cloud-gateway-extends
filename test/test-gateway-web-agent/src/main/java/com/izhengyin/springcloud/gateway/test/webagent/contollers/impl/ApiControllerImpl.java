package com.izhengyin.springcloud.gateway.test.webagent.contollers.impl;

import com.izhengyin.springcloud.gateway.test.webagent.contollers.ApiController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-12-18 16:00
 */
@RestController
@RequestMapping("/api/v1")
public class ApiControllerImpl implements ApiController {

    @Override
    public String hello(String name){
        return "hello "+name;
    }

    @Override
    public String getResourceById(@PathVariable int id){
        return "getResourceById -> "+id;
    }
}