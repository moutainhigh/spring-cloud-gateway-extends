package com.izhengyin.springcloud.gateway.base.utils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2019-12-13 15:49
 */
public class WebClientBuildUtils {
    /**
     * @param builder
     * @param connectTimeout 连接超时
     * @param readTimeout 读取超时  (未生效，待测试)
     * @param writeTimeout 写超时
     * @return
     */
    public static WebClient.Builder withTimeout(WebClient.Builder builder , int connectTimeout , int readTimeout , int writeTimeout){
        Consumer<Connection> doOnConnectedConsumer = connection -> {
            connection.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS));
        };
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .doOnConnected(doOnConnectedConsumer);
        return builder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)));
    }

}
