package com.izhengyin.springcloud.gateway.base.utils;
import java.time.Instant;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-30 12:05
 */
public class UnixTimeStampUtils {

    private final static long DAT_SECOND = 86400;
    private final static long MINUTE_SECOND = 60;

    /**
     * 获取Unix时间戳的分钟数
     * @return
     */
    public static long getMinute(){
        return Instant.now().getEpochSecond() / MINUTE_SECOND;
    }

    /**
     * 获取Unix时间戳的天数
     * @return
     */
    public static long getDay(){
        return Instant.now().getEpochSecond() / DAT_SECOND;
    }
}
