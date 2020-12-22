package com.izhengyin.springcloud.gateway.rate.limiter;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-18 17:41
 */
@Slf4j
class RateLimiterRedisScripts {

    /**
     *
     * @param unitTime
     * @return
     */
    @SuppressWarnings("unchecked")
    static RedisScript<List<Long>> getRateLimiterScript(int unitTime) {
        int minTtlTime = unitTime * 2;
        try {
            String scriptText = getRateLimiterScriptContent()
                    .replace("#{unitTime}", unitTime + "")
                    .replace("#{minTtlTime}", minTtlTime + "");
            DefaultRedisScript redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(scriptText);
            redisScript.setResultType(List.class);
            return redisScript;
        } catch (IOException e) {
            log.error("getRedisScriptContent {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取限流脚本的文本
     * @return
     */
    private static String getRateLimiterScriptContent() throws IOException {
        InputStream inputStream = null;
        InputStreamReader streamReader = null;
        try {
            inputStream = new ClassPathResource("scripts/request_rate_limiter_for_minute.lua").getInputStream();
            streamReader = new InputStreamReader(inputStream, Charsets.UTF_8);
            return CharStreams.toString(streamReader);
        }finally {
            if(Objects.nonNull(streamReader)){
                streamReader.close();
            }
            if(Objects.nonNull(inputStream)){
                inputStream.close();
            }
        }

    }
}
