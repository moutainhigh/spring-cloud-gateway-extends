package com.izhengyin.springcloud.gateway.test.webagent.contollers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @GetMapping(value = "/hello/{name}",name = "你好XXX")
    String hello(String name);

    /**
     * getResourceById
     * @param id
     * @return
     */
    @GetMapping(value = "/resource/{id}",name = "通过ID获取资源")
    String getResourceById(@PathVariable int id);
}
