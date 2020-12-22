package com.izhengyin.springcloud.gateway.rate.limiter;

import com.izhengyin.springcloud.gateway.base.utils.HttpUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-27 16:23
 */
public class RateLimiterUtils {
    private static final String KEY_PREFIX = "GatewayRateLimiter";
    /**
     * 获取限制的Key
     * @param rule
     * @param swe
     * @return
     */
    static String getLimitScriptKey(RateLimiterRule rule , ServerWebExchange swe){
        StringBuilder keyBuilder = new StringBuilder();
        String authenticationTypeKey = getAuthenticationTypeKey(rule.getAuthenticationType(),swe);
        if(StringUtils.isEmpty(authenticationTypeKey)){
            return null;
        }
        return keyBuilder.append(KEY_PREFIX)
                .append("#")
                .append(rule.getName())
                .append("#")
                .append(rule.getSource())
                .append("#")
                .append(authenticationTypeKey).toString();
    }

    /**
     * 获取限流脚本参数
     * @param rule 限流配置
     * @return
     */
    static List<String> getLimitScriptArgs(RateLimiterRule rule){
        return getLimitScriptArgs(rule,1);
    }

    /**
     * 获取限流参数
     * @param rule 限流配置
     * @param requested 请求数
     * @return
     */
    static List<String> getLimitScriptArgs(RateLimiterRule rule , int requested){
        return Arrays.asList(
                rule.getRate() + "",
                rule.getCapacity() + "",
                getLimitTimestamp(rule.getTimeWindow()) + "",
                requested +""
        );
    }

    /**
     *
     * @param type
     * @param swe
     * @return
     */
    private static String getAuthenticationTypeKey(AuthenticationType type , ServerWebExchange swe){
        String clientIp = HttpUtils.getClientIp(swe.getRequest());
        if(HttpUtils.UNKNOWN.equals(clientIp) || HttpUtils.LOCAL_IP.equals(clientIp)){
            return null;
        }
        //检查类型为爬虫
        if(AuthenticationType.SPIDER.equals(type)){
            //不是爬虫，不做验证
            String spider = SpiderUtils.getDeclarativeSpider(HttpUtils.getUserAgent(swe.getRequest()));
            if(StringUtils.isEmpty(spider)){
                return null;
            }
            return spider;
        }else if(AuthenticationType.IP.equals(type)) {
            return clientIp;
        }else if(AuthenticationType.IP_A.equals(type)){
            return Stream.of(clientIp.split("\\."))
                    .limit(1)
                    .collect(Collectors.joining("."));
        }else if(AuthenticationType.IP_B.equals(type)){
            return Stream.of(clientIp.split("\\."))
                    .limit(2)
                    .collect(Collectors.joining("."));
        }else if(AuthenticationType.IP_C.equals(type)){
            return Stream.of(clientIp.split("\\."))
                    .limit(3)
                    .collect(Collectors.joining("."));
        }
        throw new IllegalArgumentException("AuthenticationType is illegal! "+type);
    }

    /**
     * 根据不同的时间窗口获取限流的时间戳
     * @param timeWindow
     * @return
     */
    private static long getLimitTimestamp(TimeWindow timeWindow){
        return (Instant.now().getEpochSecond()/timeWindow.getUnitTime());
    }

    /**
     * 爬虫识别工具类
     */
    private static class SpiderUtils{
        private static final String BAIDU_SPIDER = "Baiduspider";
        private static final String SPIDER_360 = "360Spider";
        private static final String BYTE_SPIDER = "Bytespider";
        private static final String BING_BOT = "bingbot";
        private static final String GOOGLE_BOT = "Googlebot";
        private static final String SOGOU_WEB = "Sogou web spider";
        private static final List<String> DECLARATIVE_SPIDER_LIST = Arrays.asList(
                BAIDU_SPIDER,
                SPIDER_360,
                BYTE_SPIDER,
                BING_BOT,
                GOOGLE_BOT,
                SOGOU_WEB
        );
        private static final List<String> OTHER_SPIDER_KEY_WORDS = Arrays.asList("spider","Spider","bot","Bot");

        /**
         * 获取声明的爬虫
         * @param ua
         * @return
         */
        private static String getDeclarativeSpider(String ua){
            if(ua == null){
                return "";
            }
            for (String keyWord : DECLARATIVE_SPIDER_LIST){
                if(ua.contains(keyWord)){
                    return keyWord;
                }
            }
            for (String keyWord : OTHER_SPIDER_KEY_WORDS){
                if(ua.contains(keyWord)){
                    return "other-spider";
                }
            }
            return "";
        }
    }


}
