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

package top.panson.example.core.config;

import top.panson.core.executor.DynamicThreadPool;
import top.panson.core.executor.SpringDynamicThreadPool;
import top.panson.core.executor.support.ThreadPoolBuilder;
import top.panson.example.core.handler.TaskTraceBuilderHandler;
import com.alibaba.ttl.threadpool.TtlExecutors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static top.panson.example.core.constant.GlobalTestConstant.MESSAGE_CONSUME;
import static top.panson.example.core.constant.GlobalTestConstant.MESSAGE_PRODUCE;

/**
 *
 * @方法描述：在这个例子中创建了三个动态线程池对象交给spring容器了
 */
@Slf4j
@Configuration
public class DynamicThreadPoolConfig {

    @Bean
    @DynamicThreadPool
    public Executor messageConsumeTtlDynamicThreadPool() {
        String threadPoolId = MESSAGE_CONSUME;
        ThreadPoolExecutor customExecutor = ThreadPoolBuilder.builder()
                .dynamicPool()
                .threadFactory(threadPoolId)
                .threadPoolId(threadPoolId)
                .executeTimeOut(800L)
                .waitForTasksToCompleteOnShutdown(true)
                .awaitTerminationMillis(5000L)
                .taskDecorator(new TaskTraceBuilderHandler())
                .build();
        // Ali ttl adaptation use case.
        Executor ttlExecutor = TtlExecutors.getTtlExecutor(customExecutor);
        return ttlExecutor;
    }

    @Bean
    @DynamicThreadPool
    public Executor testConsumeTtlDynamicThreadPool() {
        String threadPoolId = "test-consume";
        ThreadPoolExecutor customExecutor = ThreadPoolBuilder.builder()
                .dynamicPool()
                .threadFactory(threadPoolId)
                .threadPoolId(threadPoolId)
                .executeTimeOut(800L)
                .waitForTasksToCompleteOnShutdown(true)
                .awaitTerminationMillis(5000L)
                .taskDecorator(new TaskTraceBuilderHandler())
                .build();
        // Ali ttl adaptation use case.
        Executor ttlExecutor = TtlExecutors.getTtlExecutor(customExecutor);
        return ttlExecutor;
    }


    /**
     * {@link Bean @Bean} and {@link DynamicThreadPool @DynamicThreadPool}.
     */
    @SpringDynamicThreadPool
    public ThreadPoolExecutor messageProduceDynamicThreadPool() {
        return ThreadPoolBuilder.buildDynamicPoolById(MESSAGE_PRODUCE);
    }

}
