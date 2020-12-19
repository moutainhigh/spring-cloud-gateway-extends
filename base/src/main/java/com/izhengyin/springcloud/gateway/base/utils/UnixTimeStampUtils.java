package com.izhengyin.springcloud.gateway.base.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 获取上一天Unix时间戳的分钟数
     * @return
     */
    public static List<Long> getBeforeDayMinutes(){
        List<Long> minutes = new ArrayList<>();
        long dayBeginSecond = Instant.now().getEpochSecond() - Instant.now().getEpochSecond() % DAT_SECOND;
        for (long start = dayBeginSecond - DAT_SECOND + MINUTE_SECOND ; start <= dayBeginSecond; start += MINUTE_SECOND){
            minutes.add(start / MINUTE_SECOND);
        }
        return minutes;
    }

    /**
     * 获取文本时间
     * @param epochSecond
     * @return
     */
    public static String getDatetimeTxt(int epochSecond){
        LocalDateTime t = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.of("UTC"));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return t.format(fmt);
    }


}
