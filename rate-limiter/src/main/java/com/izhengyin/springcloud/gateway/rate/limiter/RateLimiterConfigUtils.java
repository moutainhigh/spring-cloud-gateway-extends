package com.izhengyin.springcloud.gateway.rate.limiter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-28 11:42
 */
@Slf4j
public class RateLimiterConfigUtils {
    private final Map<String, Integer> wordsMap = new HashMap<>();
    private final TireTree tireTreeRoot = new TireTree(null,null);
    private final Map<String,Config> configs;

    RateLimiterConfigUtils(Map<String,Config> configs){
        this.configs = configs;
    }

    /**
     * 查找所有的配置
     * @param name
     * @return
     */
    public List<RateLimiterRule> findRateLimiterRules(String name){
        List<Config> configs = findAllConfig(name);
        if(Objects.isNull(configs) || configs.isEmpty()){
            return new ArrayList<>();
        }
        return configs.stream()
                .flatMap(config -> config.getRules().stream())
                .collect(Collectors.toList());
    }

    /**
     * 刷新
     */
    @PostConstruct
    void postConstruct(){
        log.info("postConstruct , "+this.getClass().getName());
        loadWordsMap();
        loadConfigToTireTree();
    }

    /**
     * 初始化 words map
     * @return {@link Map}
     */
    private void loadWordsMap(){
        List<String> words = this.configs.keySet()
                .stream()
                .filter(Objects::nonNull)
                .flatMap(v -> Stream.of(v.split("\\.")))
                .distinct()
                .collect(Collectors.toList());
        Map<String, Integer> map = new HashMap<>(words.size());
        int index = 0;
        for(String word : words){
            map.put(word,index);
            index ++;
        }
        this.wordsMap.clear();
        this.wordsMap.putAll(map);
        log.info("loadWordsMap -> "+JSON.toJSONString(new TreeMap<String, Integer>(wordsMap), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
    }

    /**
     * 初始化 tire tree
     * @return
     */
    private void loadConfigToTireTree(){
        this.tireTreeRoot.setConfig(null);
        this.tireTreeRoot.setNodes(new TireTree[wordsMap.size()]);
        this.configs.forEach(this::insert);
        log.info("loadConfigToTireTree -> "+JSON.toJSONString(tireTreeRoot, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
    }

    /**
     * 查找所有的配置
     * @param name
     * @return
     */
    private List<Config> findAllConfig(String name){
        Objects.requireNonNull(name,"name cannot be null!");
        String[] words = name.split("\\.");
        int len = words.length;
        TireTree currentNode = tireTreeRoot;
        List<Config> configs = new ArrayList<>();
        for (int n = 0; n < len ; n ++){
            Integer i = wordsMap.get(words[n]);
            if(Objects.isNull(i)){
                break;
            }
            if(currentNode.getNodes()[i] != null){
                currentNode = currentNode.getNodes()[i];
                if(currentNode.getConfig() != null){
                    configs.add(currentNode.getConfig());
                }
            }
        }
        return configs;
    }


    /**
     * 插入数据
     * @param name
     * @param config
     */
    private void insert(String name , Config config){
        Objects.requireNonNull(name,"initTireTree "+name+" cannot be null!");
        String[] words = name.split("\\.");
        int len = words.length;
        TireTree currentNode = tireTreeRoot;
        for (int n = 0; n < len ; n ++){
            String word = words[n];
            int i = wordsMap.get(word);
            Config c = null;
            //词尾
            if(n == len - 1){
                c = config;
            }
            //节点不存在，创建并赋值
            if(currentNode.getNodes()[i] == null){
                currentNode.getNodes()[i] = new TireTree(c,new TireTree[wordsMap.size()]);
            }
            //节点已存在，检查 config 是否存在，存在设置 （原因是在上一次配置insert中已创建，比如先创建的是  com.izhengyin.xxx.abc , 当前创建的是 com.izhengyin.xxx）
            else if(currentNode.getNodes()[i] != null && c != null){
                currentNode.getNodes()[i].setConfig(c);
            }
            currentNode = currentNode.getNodes()[i];
        }
    }

    @Data
    @ToString
    @AllArgsConstructor
    private class TireTree{
        private Config config;
        private TireTree[] nodes;
    }

}
