package com.izhengyin.springcloud.gateway.example.webagent.contollers;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-12-18 15:59
 */
public interface ApiController {

    /**
     * hello
     * @param name
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.HEAD},value = {"/hello/{name}","/hi/{name}"},name = "你好XXX")
    String hello(@PathVariable String name);

    /**
     * getResourceById
     * @param id
     * @return
     */
    @GetMapping(value = "/resource/{id}",name = "通过ID获取资源")
    String getResourceById(@PathVariable int id);
}
