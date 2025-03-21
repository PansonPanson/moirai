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

package top.panson.adapter.web;

import top.panson.common.constant.ChangeThreadPoolConstants;
import top.panson.common.model.ThreadPoolBaseInfo;
import top.panson.common.model.ThreadPoolParameter;
import top.panson.common.model.ThreadPoolParameterInfo;
import top.panson.common.model.ThreadPoolRunStateInfo;
import top.panson.common.toolkit.CalculateUtil;
import top.panson.core.executor.state.AbstractThreadPoolRuntime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/9
 * @方法描述：tomcat线程池处理器，这个处理器主要负责收集tomcat线程池的信息，以及动态刷新tomcat线程池的配置信息
 */
@Slf4j
@RequiredArgsConstructor
public class TomcatWebThreadPoolHandler extends AbstractWebThreadPoolService {

    //处理tomcat线程池过程中是否出现异常的标志，默认没有
    private final AtomicBoolean cacheFlag = new AtomicBoolean(Boolean.FALSE);

    //接收异常的字符串
    private static String EXCEPTION_MESSAGE;


    private final AbstractThreadPoolRuntime webThreadPoolRunStateHandler;


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/9
     * @方法描述：得到tomcat线程池的方法
     */
    @Override
    protected Executor getWebThreadPoolByServer(WebServer webServer) {
        if (cacheFlag.get()) {
            log.warn("Exception getting Tomcat thread pool. Exception message: {}", EXCEPTION_MESSAGE);
            return null;
        }
        Executor tomcatExecutor = null;
        try {
            tomcatExecutor = ((TomcatWebServer) webServer).getTomcat().getConnector().getProtocolHandler().getExecutor();
        } catch (Exception ex) {
            cacheFlag.set(Boolean.TRUE);
            EXCEPTION_MESSAGE = ex.getMessage();
            log.error("Failed to get Tomcat thread pool. Message: {}", EXCEPTION_MESSAGE);
        }
        return tomcatExecutor;
    }

    @Override
    public ThreadPoolBaseInfo simpleInfo() {
        ThreadPoolBaseInfo poolBaseInfo = new ThreadPoolBaseInfo();
        org.apache.tomcat.util.threads.ThreadPoolExecutor tomcatThreadPoolExecutor = (org.apache.tomcat.util.threads.ThreadPoolExecutor) executor;
        int corePoolSize = tomcatThreadPoolExecutor.getCorePoolSize();
        int maximumPoolSize = tomcatThreadPoolExecutor.getMaximumPoolSize();
        long keepAliveTime = tomcatThreadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS);
        BlockingQueue<?> blockingQueue = tomcatThreadPoolExecutor.getQueue();
        int queueSize = blockingQueue.size();
        int remainingCapacity = blockingQueue.remainingCapacity();
        int queueCapacity = queueSize + remainingCapacity;
        String rejectedExecutionHandlerName = executor instanceof ThreadPoolExecutor ? ((ThreadPoolExecutor) executor).getRejectedExecutionHandler().getClass().getSimpleName()
                : tomcatThreadPoolExecutor.getRejectedExecutionHandler().getClass().getSimpleName();
        poolBaseInfo.setCoreSize(corePoolSize);
        poolBaseInfo.setMaximumSize(maximumPoolSize);
        poolBaseInfo.setKeepAliveTime(keepAliveTime);
        poolBaseInfo.setQueueType(blockingQueue.getClass().getSimpleName());
        poolBaseInfo.setQueueCapacity(queueCapacity);
        poolBaseInfo.setRejectedName(rejectedExecutionHandlerName);
        return poolBaseInfo;
    }

    @Override
    public ThreadPoolParameter getWebThreadPoolParameter() {
        ThreadPoolParameterInfo parameterInfo = new ThreadPoolParameterInfo();
        try {
            org.apache.tomcat.util.threads.ThreadPoolExecutor tomcatThreadPoolExecutor = (org.apache.tomcat.util.threads.ThreadPoolExecutor) executor;
            int minThreads = tomcatThreadPoolExecutor.getCorePoolSize();
            int maxThreads = tomcatThreadPoolExecutor.getMaximumPoolSize();
            long keepAliveTime = tomcatThreadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS);
            parameterInfo.setCoreSize(minThreads);
            parameterInfo.setMaxSize(maxThreads);
            parameterInfo.setKeepAliveTime((int) keepAliveTime);
        } catch (Exception ex) {
            log.error("Failed to get the tomcat thread pool parameter.", ex);
        }
        return parameterInfo;
    }

    @Override
    public ThreadPoolRunStateInfo getWebRunStateInfo() {
        if (executor instanceof ThreadPoolExecutor) {
            return webThreadPoolRunStateHandler.getPoolRunState(null, executor);
        }
        ThreadPoolRunStateInfo runStateInfo = new ThreadPoolRunStateInfo();
        org.apache.tomcat.util.threads.ThreadPoolExecutor tomcatThreadPoolExecutor = (org.apache.tomcat.util.threads.ThreadPoolExecutor) executor;
        int corePoolSize = tomcatThreadPoolExecutor.getCorePoolSize();
        int maximumPoolSize = tomcatThreadPoolExecutor.getMaximumPoolSize();
        int poolSize = tomcatThreadPoolExecutor.getPoolSize();
        int activeCount = tomcatThreadPoolExecutor.getActiveCount();
        int largestPoolSize = tomcatThreadPoolExecutor.getLargestPoolSize();
        long completedTaskCount = tomcatThreadPoolExecutor.getCompletedTaskCount();
        String currentLoad = CalculateUtil.divide(activeCount, maximumPoolSize) + "";
        String peakLoad = CalculateUtil.divide(largestPoolSize, maximumPoolSize) + "";
        BlockingQueue<Runnable> queue = tomcatThreadPoolExecutor.getQueue();
        int queueSize = queue.size();
        String queueType = queue.getClass().getSimpleName();
        int remainingCapacity = queue.remainingCapacity();
        int queueCapacity = queueSize + remainingCapacity;
        runStateInfo.setCoreSize(corePoolSize);
        runStateInfo.setPoolSize(poolSize);
        runStateInfo.setMaximumSize(maximumPoolSize);
        runStateInfo.setActiveSize(activeCount);
        runStateInfo.setCurrentLoad(currentLoad);
        runStateInfo.setPeakLoad(peakLoad);
        runStateInfo.setQueueType(queueType);
        runStateInfo.setQueueSize(queueSize);
        runStateInfo.setQueueCapacity(queueCapacity);
        runStateInfo.setQueueRemainingCapacity(remainingCapacity);
        runStateInfo.setLargestPoolSize(largestPoolSize);
        runStateInfo.setCompletedTaskCount(completedTaskCount);
        runStateInfo.setClientLastRefreshTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        runStateInfo.setTimestamp(System.currentTimeMillis());
        String rejectedExecutionHandlerName = executor instanceof ThreadPoolExecutor ? ((ThreadPoolExecutor) executor).getRejectedExecutionHandler().getClass().getSimpleName()
                : tomcatThreadPoolExecutor.getRejectedExecutionHandler().getClass().getSimpleName();
        runStateInfo.setRejectedName(rejectedExecutionHandlerName);
        return webThreadPoolRunStateHandler.supplement(runStateInfo);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/9
     * @方法描述：更新tomcat线程池配置信息的方法
     */
    @Override
    public void updateWebThreadPool(ThreadPoolParameterInfo threadPoolParameterInfo) {
        try {
            org.apache.tomcat.util.threads.ThreadPoolExecutor tomcatThreadPoolExecutor = (org.apache.tomcat.util.threads.ThreadPoolExecutor) executor;
            int originalCoreSize = tomcatThreadPoolExecutor.getCorePoolSize();
            int originalMaximumPoolSize = tomcatThreadPoolExecutor.getMaximumPoolSize();
            long originalKeepAliveTime = tomcatThreadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS);
            tomcatThreadPoolExecutor.setCorePoolSize(threadPoolParameterInfo.corePoolSizeAdapt());
            tomcatThreadPoolExecutor.setMaximumPoolSize(threadPoolParameterInfo.maximumPoolSizeAdapt());
            tomcatThreadPoolExecutor.setKeepAliveTime(threadPoolParameterInfo.getKeepAliveTime(), TimeUnit.SECONDS);
            log.info("[Tomcat] Changed web thread pool. corePoolSize: {}, maximumPoolSize: {}, keepAliveTime: {}",
                    String.format(ChangeThreadPoolConstants.CHANGE_DELIMITER, originalCoreSize, threadPoolParameterInfo.corePoolSizeAdapt()),
                    String.format(ChangeThreadPoolConstants.CHANGE_DELIMITER, originalMaximumPoolSize, threadPoolParameterInfo.maximumPoolSizeAdapt()),
                    String.format(ChangeThreadPoolConstants.CHANGE_DELIMITER, originalKeepAliveTime, threadPoolParameterInfo.getKeepAliveTime()));
        } catch (Exception ex) {
            log.error("Failed to modify the Tomcat thread pool parameter.", ex);
        }
    }
}
