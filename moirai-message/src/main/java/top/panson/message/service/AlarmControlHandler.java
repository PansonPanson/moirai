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

package top.panson.message.service;

import top.panson.common.constant.Constants;
import top.panson.common.toolkit.StringUtil;
import top.panson.message.dto.AlarmControlDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/10
 * @方法描述：告警控制器，就是在程序想要发送告警信息的时候，判断一下，是否符合发送频率，不能发送太频繁了，如果符合发送频率，那就给用户发送信息
 */
public class AlarmControlHandler {

    private final Map<String, ReentrantLock> threadPoolLock = new HashMap<>();

    private final Map<String, Cache<String, String>> threadPoolAlarmCache = new ConcurrentHashMap<>();

    
    /** 
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。 
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/10
     * @方法描述：是否可以发送告警信息的方法
     */ 
    public boolean isSendAlarm(AlarmControlDTO alarmControl) {
        //得到组合key
        String threadPoolKey = alarmControl.buildPk();
        //从map中得到该线程池对应的缓存对象，注意，如果是第一次发送告警信息，这个时候缓存信息中应该还没有写入任何数据呢
        Cache<String, String> cache = threadPoolAlarmCache.get(threadPoolKey);
        if (cache == null) {
            return false;
        }//如果是第一次发告警信息，这里得到的key就是null
        String pkId = cache.getIfPresent(alarmControl.getTypeEnum().name());
        if (StringUtil.isBlank(pkId)) {
            //如果上面得到的pkId为空，说明现在可以发送告警信息，在这里得到同步锁
            ReentrantLock lock = threadPoolLock.get(threadPoolKey);
            //上锁
            lock.lock();
            try {//双重判断，如果缓存中的key仍然为空，就意味着可以发送告警信息
                pkId = cache.getIfPresent(alarmControl.getTypeEnum().name());
                if (StringUtil.isBlank(pkId)) {
                    //并且在这里向缓存中存放一个键，这个键会在告警信息的发送频率时间过去之后才会过期
                    //这时候大家应该就明白了，只要这个缓存中有数据，就不能发送告警信息，这就意味着发送频率没过呢
                    //如果缓存中没有数据了，意味着已经过了缓存频率了，可以发送下一次数据了
                    cache.put(alarmControl.getTypeEnum().name(), "-");
                    return true;
                }
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    
    
    /** 
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。 
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/10
     * @方法描述：在这里把从服务端收集到的线程池对应的告警信息缓存在了threadPoolAlarmCache中
     */ 
    public void initCacheAndLock(String threadPoolId, String platform, Integer interval) {
        //得到组合key
        String threadPoolKey = threadPoolId + Constants.GROUP_KEY_DELIMITER + platform;
        //这里根据线程池告警信息的发送频率得到了一个缓存对象，并且设置了缓存对象中写入数据后，数据的过期时间
        Cache<String, String> cache = Caffeine.newBuilder()
                //过期时间就是发送频率
                .expireAfterWrite(interval, TimeUnit.MINUTES)
                .build();
        //存放到map中
        threadPoolAlarmCache.put(threadPoolKey, cache);
        //这里可以看到，又创建了一个同步锁，这个同步锁会在本类的isSendAlarm方法中被用到
        ReentrantLock reentrantLock = new ReentrantLock();
        //同步锁以key-value的方式缓存到了threadPoolLock中
        threadPoolLock.put(threadPoolKey, reentrantLock);
    }
}
