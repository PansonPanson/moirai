/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.panson.config.service;

import top.panson.common.config.ApplicationContextHolder;
import top.panson.common.constant.Constants;
import top.panson.common.design.observer.AbstractSubjectCenter;
import top.panson.common.design.observer.Observer;
import top.panson.common.design.observer.ObserverMessage;
import top.panson.common.toolkit.*;
import top.panson.config.event.LocalDataChangeEvent;
import top.panson.config.model.CacheItem;
import top.panson.config.model.ConfigAllInfo;
import top.panson.config.notify.NotifyCenter;
import top.panson.config.service.biz.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static top.panson.common.constant.Constants.GROUP_KEY_DELIMITER;
import static top.panson.common.constant.Constants.GROUP_KEY_DELIMITER_TRANSLATION;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：服务端缓存线程池信息的类
 */
@Slf4j
public class ConfigCacheService {

    //这个就是ConfigServiceImpl，用来和数据库打交道的
    private static ConfigService CONFIG_SERVICE;

    //注册一个ClearConfigCache对象，在发布缓存信息移除的事件时，会回调ClearConfigCache对象中的方法，清空缓存组件中缓存的信息
    //在第四版本代码中，这个组件具体被调用的逻辑可以从BaseInstanceRegistry类的internalCancel方法查看
    static {
        AbstractSubjectCenter.register(AbstractSubjectCenter.SubjectType.CLEAR_CONFIG_CACHE, new ClearConfigCache());
    }


    //缓存线程池配置信息的map
    //假如以客户端提供的三个线程池中的那个test-consume线程池为例，这个map中存放的数据就为
    //key：test-consume+dynamic-threadpool-example+prescription+127.0.0.1：8088_2226ea7a3d524a9e983f1fafac5df0e
    //value就是CacheItemMap，而CacheItemMap中封装的数据是这样的,key:127.0.0.1：8088_2226ea7a3d524a9e983f1fafac5df0e value就是CacheItem本身，而CacheItem封装的数据是这样的
    //CacheItem.groupKey:test-consume+dynamic-threadpool-example+prescription+127.0.0.1：8088_2226ea7a3d524a9e983f1fafac5df0e
    //CacheItem.md5:这个值大家自己打断点看吧
    //CacheItem.configAllInfo:这个就是封装了线程池配置信息的对象
    private static final ConcurrentHashMap<String, Map<String, CacheItem>> CLIENT_CONFIG_CACHE = new ConcurrentHashMap();

    //判断服务端线程池信息是否更新了的方法
    public static boolean isUpdateData(String groupKey, String md5, String clientIdentify) {
        //从缓存线程池信息的服务中获取对应的客户端要监听的线程池信息
        String contentMd5 = ConfigCacheService.getContentMd5IsNullPut(groupKey, clientIdentify);
        //在这里比较客户端和服务端线程池md5是否相同
        return Objects.equals(contentMd5, md5);
    }


    public static boolean checkTpId(String groupKey, String tpId, String clientIdentify) {
        Map<String, CacheItem> cacheItemMap = Optional.ofNullable(CLIENT_CONFIG_CACHE.get(groupKey)).orElse(new HashMap<>());
        CacheItem cacheItem;
        if (CollectionUtil.isNotEmpty(cacheItemMap) && (cacheItem = cacheItemMap.get(clientIdentify)) != null) {
            return Objects.equals(tpId, cacheItem.configAllInfo.getTpId());
        }
        return Boolean.FALSE;
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：获取服务端对应线程池的md5的值，这里我多说一句，下面这些方法的逻辑都非常简单，但是有很多解析key或者组合key然后查询的操作
     * 我建议大家在看这一块代码的时候打个断点慢慢看，我当时看的时候也是打了断点，我就不再注释中给大家写明这些key或者value的值是什么了，这样就太麻烦了
     * 而且这也不是什么难点，反而是一些很简单的逻辑，就留给大家自己看看吧
     */
    private synchronized static String getContentMd5IsNullPut(String groupKey, String clientIdentify) {
        //首先从缓存信息的map中获取
        Map<String, CacheItem> cacheItemMap = Optional.ofNullable(CLIENT_CONFIG_CACHE.get(groupKey)).orElse(new HashMap<>());
        CacheItem cacheItem = null;
        //获取之后判断数据是否不为空，不为空则直接返回
        if (CollectionUtil.isNotEmpty(cacheItemMap) && (cacheItem = cacheItemMap.get(clientIdentify)) != null) {
            return cacheItem.md5;
        }
        //走到这里意味着缓存中没有数据，那就得到ConfigServiceImpl对象
        if (CONFIG_SERVICE == null) {
            CONFIG_SERVICE = ApplicationContextHolder.getBean(ConfigService.class);
        }
        String[] params = groupKey.split(GROUP_KEY_DELIMITER_TRANSLATION);
        //从数据库中查询最新的配置信息
        ConfigAllInfo config = CONFIG_SERVICE.findConfigRecentInfo(params);
        //如果数据不为空，就先更新CLIENT_CONFIG_CACHE这个缓存的内容
        if (config != null && StringUtil.isNotBlank(config.getTpId())) {
            cacheItem = new CacheItem(groupKey, config);
            cacheItemMap.put(clientIdentify, cacheItem);
            CLIENT_CONFIG_CACHE.put(groupKey, cacheItemMap);
        }//在这里返回最新的md5的值
        return (cacheItem != null) ? cacheItem.md5 : Constants.NULL;
    }

    //得到key对应的md5的值
    public static String getContentMd5(String groupKey) {
        if (CONFIG_SERVICE == null) {
            CONFIG_SERVICE = ApplicationContextHolder.getBean(ConfigService.class);
        }
        String[] params = groupKey.split(GROUP_KEY_DELIMITER_TRANSLATION);
        ConfigAllInfo config = CONFIG_SERVICE.findConfigRecentInfo(params);
        if (config == null || StringUtils.isEmpty(config.getTpId())) {
            String errorMessage = String.format("config is null. tpId: %s, itemId: %s, tenantId: %s", params[0], params[1], params[2]);
            throw new RuntimeException(errorMessage);
        }
        return Md5Util.getTpContentMd5(config);
    }


    //更新服务端缓存的线程池的md5的值
    public static void updateMd5(String groupKey, String identify, String md5) {
        CacheItem cache = makeSure(groupKey, identify);
        if (cache.md5 == null || !cache.md5.equals(md5)) {
            cache.md5 = md5;
            String[] params = groupKey.split(GROUP_KEY_DELIMITER_TRANSLATION);
            ConfigAllInfo config = CONFIG_SERVICE.findConfigRecentInfo(params);
            cache.configAllInfo = config;
            cache.lastModifiedTs = System.currentTimeMillis();
            NotifyCenter.publishEvent(new LocalDataChangeEvent(identify, groupKey));
        }
    }



    public synchronized static CacheItem makeSure(String groupKey, String ip) {
        Map<String, CacheItem> ipCacheItemMap = CLIENT_CONFIG_CACHE.get(groupKey);
        CacheItem item;
        if (ipCacheItemMap != null && (item = ipCacheItemMap.get(ip)) != null) {
            return item;
        }
        CacheItem tmp = new CacheItem(groupKey);
        Map<String, CacheItem> cacheItemMap = new HashMap<>();
        cacheItemMap.put(ip, tmp);
        CLIENT_CONFIG_CACHE.putIfAbsent(groupKey, cacheItemMap);
        return tmp;
    }



    public static Map<String, CacheItem> getContent(String identification) {
        List<String> identificationList = MapUtil.parseMapForFilter(CLIENT_CONFIG_CACHE, identification);
        Map<String, CacheItem> returnStrCacheItemMap = new HashMap<>();
        identificationList.forEach(each -> returnStrCacheItemMap.putAll(CLIENT_CONFIG_CACHE.get(each)));
        return returnStrCacheItemMap;
    }


    public static synchronized Integer getTotal() {
        AtomicInteger total = new AtomicInteger();
        CLIENT_CONFIG_CACHE.forEach((key, val) -> total.addAndGet(val.values().size()));
        return total.get();
    }


    public static List<String> getIdentifyList(String tenantId, String itemId, String threadPoolId) {
        List<String> identifyList = null;
        String buildKey = Joiner.on(GROUP_KEY_DELIMITER).join(CollectionUtil.newArrayList(threadPoolId, itemId, tenantId));
        List<String> keys = MapUtil.parseMapForFilter(CLIENT_CONFIG_CACHE, buildKey);
        if (CollectionUtil.isNotEmpty(keys)) {
            identifyList = new ArrayList<>(keys.size());
            for (String each : keys) {
                String[] keyArray = each.split(GROUP_KEY_DELIMITER_TRANSLATION);
                if (keyArray.length > 2) {
                    identifyList.add(keyArray[3]);
                }
            }
        }
        return identifyList;
    }


    public static void removeConfigCache(String groupKey) {
        coarseRemove(groupKey);
    }


    private synchronized static void coarseRemove(String coarse) {
        List<String> identificationList = MapUtil.parseMapForFilter(CLIENT_CONFIG_CACHE, coarse);
        for (String cacheMapKey : identificationList) {
            Map<String, CacheItem> removeCacheItem = CLIENT_CONFIG_CACHE.remove(cacheMapKey);
            log.info("Remove invalidated config cache. config info: {}", JSONUtil.toJSONString(removeCacheItem));
        }
    }


    static class ClearConfigCache implements Observer<String> {


        @Override
        public void accept(ObserverMessage<String> observerMessage) {
            log.info("Clean up the configuration cache. Key: {}", observerMessage.message());
            coarseRemove(observerMessage.message());
        }
    }
}
