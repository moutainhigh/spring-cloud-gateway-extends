package com.izhengyin.springcloud.gateway.base.config;
import com.izhengyin.springcloud.gateway.base.ApiManageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-19 10:07
 */
@Configuration
public class GatewayRedisMessageListenerConfiguration {

    private final ApiManageService apiManageService;

    public GatewayRedisMessageListenerConfiguration(ApiManageService apiManageService){
        this.apiManageService = apiManageService;
    }

    @Bean
    MessageListenerAdapter messageListenerAdapter() {
        return new MessageListenerAdapter(apiManageService.new GatewayRedisMessageListener());
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(apiManageService.SERVER_REFRESH_TOPIC));
        return container;
    }


}
