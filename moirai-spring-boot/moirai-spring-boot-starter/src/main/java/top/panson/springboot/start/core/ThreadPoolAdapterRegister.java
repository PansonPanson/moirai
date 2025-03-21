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

package top.panson.springboot.start.core;

import top.panson.adapter.base.ThreadPoolAdapter;
import top.panson.adapter.base.ThreadPoolAdapterCacheConfig;
import top.panson.adapter.base.ThreadPoolAdapterRegisterAction;
import top.panson.adapter.base.ThreadPoolAdapterState;
import top.panson.common.config.ApplicationContextHolder;
import top.panson.common.toolkit.CollectionUtil;
import top.panson.common.web.base.Result;
import top.panson.core.toolkit.IdentifyUtil;
import top.panson.core.toolkit.inet.InetUtils;
import top.panson.springboot.start.config.BootstrapProperties;
import top.panson.springboot.start.remote.HttpAgent;
import top.panson.springboot.start.toolkit.CloudCommonIdUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static top.panson.common.constant.Constants.IDENTIFY_SLICER_SYMBOL;
import static top.panson.common.constant.Constants.REGISTER_ADAPTER_PATH;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/9
 * @方法描述：dubbo等第三方线程池信息注册器，这个注册器实现了ApplicationRunner接口，接口方法回调的时候
 * 会把dubbo这些第三方线程池的信息注册到服务端，这个类的代码非常简单，大家自己看看就行
 */
@Slf4j
@AllArgsConstructor
public class ThreadPoolAdapterRegister implements ApplicationRunner, ThreadPoolAdapterRegisterAction {

    private final HttpAgent httpAgent;

    private final BootstrapProperties properties;

    private final ConfigurableEnvironment environment;

    private final InetUtils hippo4JInetUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        register();
    }

    @Override
    public List<ThreadPoolAdapterCacheConfig> getThreadPoolAdapterCacheConfigs(Map<String, ThreadPoolAdapter> threadPoolAdapterMap) {
        List<ThreadPoolAdapterCacheConfig> adapterCacheConfigList = new ArrayList<>();
        for (Map.Entry<String, ThreadPoolAdapter> threadPoolAdapterEntry : threadPoolAdapterMap.entrySet()) {
            ThreadPoolAdapter threadPoolAdapter = threadPoolAdapterEntry.getValue();
            List<ThreadPoolAdapterState> threadPoolStates = threadPoolAdapter.getThreadPoolStates();
            if (CollectionUtil.isEmpty(threadPoolStates) || threadPoolStates.size() == 0) {
                continue;
            }
            ThreadPoolAdapterCacheConfig cacheConfig = new ThreadPoolAdapterCacheConfig();
            cacheConfig.setMark(threadPoolAdapter.mark());
            String tenantItemKey = properties.getNamespace() + IDENTIFY_SLICER_SYMBOL + properties.getItemId();
            cacheConfig.setTenantItemKey(tenantItemKey);
            cacheConfig.setClientIdentify(IdentifyUtil.getIdentify());
            String clientAddress = CloudCommonIdUtil.getClientIpPort(environment, hippo4JInetUtils);
            cacheConfig.setClientAddress(clientAddress);
            cacheConfig.setThreadPoolAdapterStates(threadPoolStates);
            adapterCacheConfigList.add(cacheConfig);
        }
        return adapterCacheConfigList;
    }

    @Override
    public void doRegister(List<ThreadPoolAdapterCacheConfig> cacheConfigList) {
        if (CollectionUtil.isNotEmpty(cacheConfigList) && cacheConfigList.size() > 0) {
            try {
                Result result = httpAgent.httpPost(REGISTER_ADAPTER_PATH, cacheConfigList);
                if (!result.isSuccess()) {
                    log.warn("Failed to register third-party thread pool data.");
                }
            } catch (Throwable ex) {
                log.error("Failed to register third-party thread pool data.", ex);
            }
        }
    }

    public void register() {
        Map<String, ThreadPoolAdapter> threadPoolAdapterMap = ApplicationContextHolder.getBeansOfType(ThreadPoolAdapter.class);
        List<ThreadPoolAdapterCacheConfig> threadPoolAdapterCacheConfigs = getThreadPoolAdapterCacheConfigs(threadPoolAdapterMap);
        doRegister(threadPoolAdapterCacheConfigs);
    }
}
