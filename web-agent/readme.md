# web-agent

> Web-Agent功能非常简单，通过 Spring RequestMappingHandlerMapping 获取服务的所有接口描述，并暴露一个Endpoint用于网关拉取

## web-agent 引入

``` 
 <dependency>
    <groupId>com.izhengyin.springcloud.gateway.extends</groupId>
    <artifactId>web-agent</artifactId>
    <version>xxx</version>
</dependency>
```

## 暴露端点

``` 
    @GetMapping("/actuator/gateway/server/apis")
    public List<GatewayWebApiDefine> getApis();
```

## 示例

https://github.com/zhengyin/spring-cloud-gateway-extends/tree/master/example/web-agent-example

