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

package top.panson.springboot.start.controller;

import top.panson.adapter.base.ThreadPoolAdapter;
import top.panson.adapter.base.ThreadPoolAdapterParameter;
import top.panson.adapter.base.ThreadPoolAdapterState;
import top.panson.common.api.ClientNetworkService;
import top.panson.common.spi.DynamicThreadPoolServiceLoader;
import top.panson.common.toolkit.StringUtil;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.core.toolkit.IdentifyUtil;
import top.panson.core.toolkit.inet.InetUtils;
import top.panson.springboot.start.toolkit.CloudCommonIdUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static top.panson.adapter.base.ThreadPoolAdapterBeanContainer.THREAD_POOL_ADAPTER_BEAN_CONTAINER;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/9
 * @方法描述：接收服务端查询dubbo等线程池请求的控制器
 */
@Slf4j
@RestController
@AllArgsConstructor
public class ThreadPoolAdapterController {

    private final ConfigurableEnvironment environment;

    private final InetUtils hippo4JInetUtils;

    @GetMapping("/adapter/thread-pool/info")
    public Result<ThreadPoolAdapterState> getAdapterThreadPool(ThreadPoolAdapterParameter requestParameter) {
        ThreadPoolAdapter threadPoolAdapter = THREAD_POOL_ADAPTER_BEAN_CONTAINER.get(requestParameter.getMark());
        ThreadPoolAdapterState result = Optional.ofNullable(threadPoolAdapter).map(each -> {
            ThreadPoolAdapterState threadPoolState = each.getThreadPoolState(requestParameter.getThreadPoolKey());
            String active = environment.getProperty("spring.profiles.active", "UNKNOWN");
            threadPoolState.setActive(active.toUpperCase());
            String[] customerNetwork = DynamicThreadPoolServiceLoader.getSingletonServiceInstances(ClientNetworkService.class)
                    .stream().findFirst().map(network -> network.getNetworkIpPort(environment)).orElse(null);
            String clientAddress;
            if (customerNetwork != null) {
                clientAddress = StringUtil.newBuilder(customerNetwork[0], ":", customerNetwork[1]);
            } else {
                clientAddress = CloudCommonIdUtil.getClientIpPort(environment, hippo4JInetUtils);
            }
            threadPoolState.setClientAddress(clientAddress);
            threadPoolState.setIdentify(IdentifyUtil.getIdentify());
            return threadPoolState;
        }).orElse(null);
        return Results.success(result);
    }

    @PostMapping("/adapter/thread-pool/update")
    public Result<Void> updateAdapterThreadPool(@RequestBody ThreadPoolAdapterParameter requestParameter) {
        log.info("[{}] Change third-party thread pool data. key: {}, coreSize: {}, maximumSize: {}",
                requestParameter.getMark(), requestParameter.getThreadPoolKey(), requestParameter.getCorePoolSize(), requestParameter.getMaximumPoolSize());
        ThreadPoolAdapter threadPoolAdapter = THREAD_POOL_ADAPTER_BEAN_CONTAINER.get(requestParameter.getMark());
        Optional.ofNullable(threadPoolAdapter).ifPresent(each -> each.updateThreadPool(requestParameter));
        return Results.success();
    }
}
