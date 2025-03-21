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

package top.panson.core.executor.support.adpter;

import top.panson.common.toolkit.ReflectUtil;
import top.panson.core.executor.DynamicThreadPoolExecutor;
import top.panson.core.executor.support.ThreadPoolBuilder;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



/**
 *
 * @方法描述：ThreadPoolTaskExecutor第三方线程池的适配器，这个类我就不添加注释，逻辑真的很简单，大家类比着TransmittableThreadLocalExecutorAdapter类的功能看一下即可
 */
public class ThreadPoolTaskExecutorAdapter implements DynamicThreadPoolAdapter {


    private static final String EXECUTOR_FIELD_NAME = "threadPoolExecutor";


    private static final String WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN = "waitForTasksToCompleteOnShutdown";


    private static final String AWAIT_TERMINATION_MILLIS = "awaitTerminationMillis";


    private static final String TASK_DECORATOR = "taskDecorator";


    private static final String BEAN_NAME = "beanName";


    private static final String QUEUE_CAPACITY = "queueCapacity";


    private static String MATCH_CLASS_NAME = "ThreadPoolTaskExecutor";


    @Override
    public boolean match(Object executor) {
        return Objects.equals(MATCH_CLASS_NAME, executor.getClass().getSimpleName());
    }

    @Override
    public DynamicThreadPoolExecutor unwrap(Object executor) {
        Object unwrap = ReflectUtil.getFieldValue(executor, EXECUTOR_FIELD_NAME);
        if (unwrap == null) {
            return null;
        }
        if (!(unwrap instanceof ThreadPoolExecutor)) {
            return null;
        }
        if (unwrap instanceof DynamicThreadPoolExecutor) {
            return (DynamicThreadPoolExecutor) unwrap;
        }
        boolean waitForTasksToCompleteOnShutdown = (boolean) ReflectUtil.getFieldValue(executor, WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN);
        long awaitTerminationMillis = (long) ReflectUtil.getFieldValue(executor, AWAIT_TERMINATION_MILLIS);
        String beanName = (String) ReflectUtil.getFieldValue(executor, BEAN_NAME);
        int queueCapacity = (int) ReflectUtil.getFieldValue(executor, QUEUE_CAPACITY);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) unwrap;
        ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) executor;
        ThreadPoolBuilder threadPoolBuilder = ThreadPoolBuilder.builder()
                .dynamicPool()
                .corePoolSize(threadPoolTaskExecutor.getCorePoolSize())
                .maxPoolNum(threadPoolTaskExecutor.getMaxPoolSize())
                .keepAliveTime(threadPoolTaskExecutor.getKeepAliveSeconds())
                .timeUnit(TimeUnit.SECONDS)
                .allowCoreThreadTimeOut(threadPoolExecutor.allowsCoreThreadTimeOut())
                .waitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown)
                .awaitTerminationMillis(awaitTerminationMillis)
                .threadFactory(threadPoolExecutor.getThreadFactory())
                .threadPoolId(beanName)
                .rejected(threadPoolExecutor.getRejectedExecutionHandler());
        threadPoolBuilder.capacity(queueCapacity);
        Optional.ofNullable(ReflectUtil.getFieldValue(executor, TASK_DECORATOR))
                .ifPresent((taskDecorator) -> threadPoolBuilder.taskDecorator((TaskDecorator) taskDecorator));
        return (DynamicThreadPoolExecutor) threadPoolBuilder.build();
    }

    @Override
    public void replace(Object executor, Executor dynamicThreadPoolExecutor) {
        ReflectUtil.setFieldValue(executor, EXECUTOR_FIELD_NAME, dynamicThreadPoolExecutor);
    }
}
