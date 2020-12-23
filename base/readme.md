# Base 模块说明

> Base 模块通过调用 web-agent 暴露的端点获取后端接口定义，并存储在Redis中，下次启动时，直接从Redis中获取 。 Base 模块通过定义的filter完成对请求到接口进行映射，在此基础上进行指标的采集与日志记录。

## 接口描述

https://github.com/zhengyin/spring-cloud-gateway-extends/blob/master/base/src/main/java/com/izhengyin/springcloud/gateway/base/pojo/Api.java

数据示例
``` 
{
    "httpMethod":["GET"],
    "mappingInfo":"{GET /api/v2/resource/{id}}",
    "method":"com.izhengyin.springcloud.gateway.test.webagent.contollers.impl.ApiV2ControllerImpl#getResourceById(int)",
    "name":"通过ID获取资源",
    "pathPatterns":["/api/v2/resource/{id}"],
    "requestNum":0,
    "server":"test-web-agent"
}
```


## 指标描述
https://github.com/zhengyin/spring-cloud-gateway-extends/blob/master/base/src/main/java/com/izhengyin/springcloud/gateway/base/pojo/Metrics.java

```
{
	"clientIp": "127.0.0.1",
	"code": 200,
	"errorMessage": "nil",
	"errorType": "nil",
	"gateway": "test-gateway-base",
	"mapping": "{GET /api/v1/resource/{id}}",
	"method": "com.izhengyin.springcloud.gateway.test.webagent.contollers.impl.ApiControllerImpl#getResourceById(int)",
	"name": "通过ID获取资源",
	"reqMethod": "GET",
	"resTimeMs": 130,
	"service": "test-web-agent",
	"timestamp": 1608697068750,
	"uri": "/api/v1/resource/1",
	"userAgent": "curl/7.54.0"
}
```

## 指标采集

### 日志指标

日志指标会被日志名为（gatewayAccessLog） 的Logger输出，定义此Logger可以控制输出路径。

### Prometheus

Prometheus的指标会被记录到 gateway.request-counter-metrics-name / response-time-metrics-name:gateway_response_time 的属性配置中

``` 
 @Value("${gateway.request-counter-metrics-name:gateway_request_counter}") String gatewayRequestCounterMetricsName,
  @Value("${gateway.response-time-metrics-name:gateway_response_time}") String gatewayResponseTimeMetricsName
```

