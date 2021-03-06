package com.izhengyin.springcloud.gateway.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.izhengyin.springcloud.gateway.base.pojo.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 09:47
 */
@Slf4j
@EnableScheduling
@Service("apiManageService")
public class ApiManageService {

    public  static final String SERVER_REFRESH_TOPIC = "gateway-server-refresh";
    private static final String SERVER_API_MAP_KEY = "server-api-map";
    private static final String SERVER_API_ENDPOINT = "/actuator/gateway/server/apis";
    private static final String LB_SCHEME = "lb";
    private static final String HTTP_SCHEME = "http";
    private final GatewayProperties gatewayProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final WebClient webClient;
    private final WebClient nonLBWebClient;
    private List<Api> apis = new ArrayList<>();
    private ConcurrentHashMap<String,List<Api>> apiCaches = new ConcurrentHashMap<>();
    public ApiManageService(
            ReactiveStringRedisTemplate redisTemplate,
            GatewayProperties gatewayProperties ,
            WebClient webClient ,
            @Qualifier("nonLBWebClient") WebClient nonLBWebClient
    ){
        this.redisTemplate = redisTemplate;
        this.gatewayProperties = gatewayProperties;
        this.webClient = webClient;
        this.nonLBWebClient = nonLBWebClient;
    }

    @PostConstruct
    public void construct(){
        loadServerApis();
    }

    /**
     * ???????????????API??????
     * @return
     */
    public List<Api> getApis(){
        return apis;
    }

    /**
     * ?????????????????????API??????
     * @param server
     * @return
     */
    public List<Api> getApis(String server){
        return apiCaches.getOrDefault(server,new ArrayList<>());
    }

    /**
     * ??????????????????API??????
     * @return
     */
    public Mono<List<Map<String,Api>>> reloadAllApis(){
        return redisTemplate.delete(SERVER_API_MAP_KEY)
                .flatMap(l -> refreshApis("all"));
    }
    /**
     * ??????????????????API??????
     * @param server
     */
    public Mono<List<Map<String,Api>>> refreshApis(String server){
        //??????????????????????????????
        return Flux.fromIterable(gatewayProperties.getRoutes())
                    //???????????????????????????????????????
                    .filter(routeDefinition -> routeDefinition.getId().equals(server) || "all".equals(server))
                    //???????????????????????????
                    .flatMap(routeDefinition -> {
                        final String id = routeDefinition.getId();
                        final URI uri = routeDefinition.getUri();
                        if(LB_SCHEME.equals(uri.getScheme())){
                            return getApisByServerDiscover(id,uri.toString().replace(LB_SCHEME,HTTP_SCHEME));
                        }
                        return getApisByUrl(id,uri.toString());
                    })
                    // res[0] ??????ID ???res[1] ??????????????????JSON
                    .filter(res -> res.size() == 2 && !StringUtils.isEmpty(res.get(1)))
                    .flatMap(res -> {
                        try {
                            //???????????????????????????????????? Method ?????????map
                            Map<String,Api> apiMap = new HashMap<>(apis.size(),1);
                            Map<String,String> data = new HashMap<>(apiMap.size(),1);
                            JSON.parseObject(res.get(1),new TypeReference<List<Api>>(){})
                                    .stream()
                                    .collect(Collectors.groupingBy(Api::getMethod))
                                    .forEach((m,v) -> {
                                        Api api = v.get(0);
                                        api.setServer(res.get(0));
                                        apiMap.put(m,api);
                                        data.put(m,JSON.toJSONString(api));
                                    });
                            //???????????????Redis
                            return redisTemplate.opsForHash().putAll(SERVER_API_MAP_KEY,data)
                                    .map(bool -> {
                                        if(bool){
                                            return apiMap;
                                        }
                                        return new HashMap<String,Api>();
                                    });
                        }catch (RuntimeException e){
                            log.error("refresh parse res error , {} ",JSON.toJSONString(res));
                        }
                        return Mono.just(new HashMap<String,Api>());
                    })
                    .filter(v -> !v.isEmpty())
                    .collectList()
                    //??????????????????????????????????????????
                    .doOnSuccess(list -> redisTemplate.convertAndSend(SERVER_REFRESH_TOPIC,server).subscribe())
                    .doOnError(throwable -> log.error("Get server apis error , {} , {}",server,throwable.getMessage(),throwable));
    }

    /**
     * Redis??????????????????
     */
    public class GatewayRedisMessageListener implements MessageListener {
        @Override
        public void onMessage(Message message, byte[] pattern) {
            String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
            if(SERVER_REFRESH_TOPIC.equals(channel)){
                loadServerApis();
            }
        }
    }

    /**
     * ????????????????????????????????? Cache Apis????????????????????????????????????
     */
    public void resortApis(){
        //server map cache
        if(apis.isEmpty()){
            log.warn("resortApis apis is empty!");
            return;
        }
        List<String> routes = gatewayProperties.getRoutes().stream().map(RouteDefinition::getId).collect(Collectors.toList());
        apis.stream().collect(Collectors.groupingBy(Api::getServer))
                .forEach((server,list) -> {
                    //???????????????????????????????????????
                    if(!routes.contains(server)){
                        return;
                    }
                    List<Api> sortedList = list.stream()
                            .sorted((o1, o2) -> {
                                if(o1.getRequestNum() == o2.getRequestNum()){
                                    return  0;
                                }
                                return o1.getRequestNum() > o2.getRequestNum() ? -1 : 1;
                            })
                            .collect(Collectors.toList());
                    apiCaches.put(server,sortedList);
                    log.info("resort apis , service {} , sortedList {} ", server , JSON.toJSONString(sortedList , SerializerFeature.PrettyFormat , SerializerFeature.WriteMapNullValue , SerializerFeature.WriteDateUseDateFormat));
                });
    }

    /**
     * ??????????????????????????????????????????????????????
     * @param routeId
     * @param address
     * @return
     */
    private Mono<List<String>>  getApisByServerDiscover(String routeId , String address){
        return webClient.get()
                .uri(address + SERVER_API_ENDPOINT)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(throwable -> {
                    log.error("WebClient Get {} error , {}",address,throwable.getMessage(),throwable);
                    return Mono.just("");
                })
                .map(body -> Arrays.asList(routeId,body));
    }

    /**
     * ???????????????HTTP??????????????????????????????????????????
     * @param routeId
     * @param address
     * @return
     */
    private Mono<List<String>> getApisByUrl(String routeId , String address){
        return nonLBWebClient.get()
                .uri(address + SERVER_API_ENDPOINT)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(throwable -> {
                    log.error("getApisByUrl Get {} error , {}",address,throwable.getMessage(),throwable);
                    return Mono.just("");
                })
                .map(body -> Arrays.asList(routeId,body));
    }

    /**
     * ???????????? API ?????????
     */
    private void loadServerApis(){
        apis = redisTemplate.opsForHash()
                .entries(SERVER_API_MAP_KEY)
                .toStream()
                .map(Map.Entry::getValue)
                .map(v -> JSON.parseObject(v+"",new TypeReference<Api>(){}))
                .collect(Collectors.toList());
        resortApis();
        log.info("loadServerApi \n {} " , JSON.toJSONString(apis,SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
    }

}
