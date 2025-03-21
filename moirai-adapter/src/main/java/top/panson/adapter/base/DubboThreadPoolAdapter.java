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

package top.panson.adapter.base;

import top.panson.common.toolkit.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.Version;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.store.DataStore;
import org.apache.dubbo.common.threadpool.manager.ExecutorRepository;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static top.panson.common.constant.ChangeThreadPoolConstants.CHANGE_DELIMITER;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/9
 * @方法描述：dubbo线程池处理器，这个处理器专门收集dubbo线程池的运行信息，并且还会动态更新dubbo线程池的配置信息
 */
@Slf4j
public class DubboThreadPoolAdapter implements ThreadPoolAdapter, ApplicationListener<ApplicationStartedEvent> {

    //存放dubbo中所有线程池信息的map
    private final Map<String, ThreadPoolExecutor> DUBBO_PROTOCOL_EXECUTOR = new HashMap<>();

    @Override
    public String mark() {
        return "Dubbo";
    }


    @Override
    public ThreadPoolAdapterState getThreadPoolState(String identify) {
        ThreadPoolAdapterState threadPoolAdapterState = new ThreadPoolAdapterState();
        ThreadPoolExecutor executor = DUBBO_PROTOCOL_EXECUTOR.get(identify);
        if (executor == null) {
            log.warn("[{}] Dubbo consuming thread pool not found.", identify);
            return threadPoolAdapterState;
        }
        threadPoolAdapterState.setThreadPoolKey(identify);
        threadPoolAdapterState.setCoreSize(executor.getCorePoolSize());
        threadPoolAdapterState.setMaximumSize(executor.getMaximumPoolSize());
        return threadPoolAdapterState;
    }

    @Override
    public List<ThreadPoolAdapterState> getThreadPoolStates() {
        List<ThreadPoolAdapterState> threadPoolAdapterStates = new ArrayList<>();
        DUBBO_PROTOCOL_EXECUTOR.forEach((key, val) -> threadPoolAdapterStates.add(getThreadPoolState(String.valueOf(key))));
        return threadPoolAdapterStates;
    }

    //更新线程池信息
    @Override
    public boolean updateThreadPool(ThreadPoolAdapterParameter threadPoolAdapterParameter) {
        String threadPoolKey = threadPoolAdapterParameter.getThreadPoolKey();
        ThreadPoolExecutor executor = DUBBO_PROTOCOL_EXECUTOR.get(threadPoolAdapterParameter.getThreadPoolKey());
        if (executor == null) {
            log.warn("[{}] Dubbo consuming thread pool not found.", threadPoolKey);
            return false;
        }
        int originalCoreSize = executor.getCorePoolSize();
        int originalMaximumPoolSize = executor.getMaximumPoolSize();
        executor.setCorePoolSize(threadPoolAdapterParameter.getCorePoolSize());
        executor.setMaximumPoolSize(threadPoolAdapterParameter.getMaximumPoolSize());
        log.info("[{}] Dubbo consumption thread pool parameter change. coreSize: {}, maximumSize: {}",
                threadPoolKey,
                String.format(CHANGE_DELIMITER, originalCoreSize, executor.getCorePoolSize()),
                String.format(CHANGE_DELIMITER, originalMaximumPoolSize, executor.getMaximumPoolSize()));
        return true;
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/9
     * @方法描述：这个方法就是用来收集dubbo中线程池信息
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        boolean isLegacyVersion = false;
        String poolKey = ExecutorService.class.getName();
        if (Version.getIntVersion(Version.getVersion()) < 2070500) {
            isLegacyVersion = true;
        }
        try {
            if (isLegacyVersion) {
                DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
                Map<String, Object> executors = dataStore.get(poolKey);
                executors.forEach((key, value) -> DUBBO_PROTOCOL_EXECUTOR.put(key, (ThreadPoolExecutor) value));
                return;
            }
            ExecutorRepository executorRepository = ExtensionLoader.getExtensionLoader(ExecutorRepository.class).getDefaultExtension();
            ConcurrentMap<String, ConcurrentMap<Integer, ExecutorService>> data =
                    (ConcurrentMap<String, ConcurrentMap<Integer, ExecutorService>>) ReflectUtil.getFieldValue(executorRepository, "data");
            ConcurrentMap<Integer, ExecutorService> executorServiceMap = data.get(poolKey);
            executorServiceMap.forEach((key, value) -> DUBBO_PROTOCOL_EXECUTOR.put(String.valueOf(key), (ThreadPoolExecutor) value));
        } catch (Exception ex) {
            log.error("Failed to get Dubbo {}.X protocol thread pool", isLegacyVersion ? "2" : "3", ex);
        }
    }
}
