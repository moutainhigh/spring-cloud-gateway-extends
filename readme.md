<p align="center">
  <strong>指标采集、流量监控、组合流控</strong>
</p>
<p align="center">
  与 Spring cloud gateway 无缝集成
</p>

# Spring cloud gateway （网关） 除了路由、熔断还可以做的更多

1. 经过网关管理的每个接口的QPS、RT、错误数是多少？
2. 能不能有一个大屏可以看到流量的变化？
3. 除了针对服务的限流，能不能针对接口进行限流？
4. 限流能不能组合？除了按秒限流，能不能按照分进行限流？

# 模块

* web-agent 后端服务接口收集包
    https://github.com/zhengyin/spring-cloud-gateway-extends/tree/master/web-agent
* base 接口识别、指标采集、访问日志 
    https://github.com/zhengyin/spring-cloud-gateway-extends/tree/master/base
* rate-limiter 限流扩展，提供了接口限流、组合限流、多种时间窗口（按秒、按分）的限流
    https://github.com/zhengyin/spring-cloud-gateway-extends/tree/master/rate-limiter

