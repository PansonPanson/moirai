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

package top.panson.server.listener;

import top.panson.config.toolkit.EnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/28
 * @方法描述：启动程序的监听器，这个监听器反映了程序启动阶段的状态，在启动各个阶段输出了日志
 */
@Slf4j
public class StartingApplicationListener implements Hippo4JApplicationListener {

    private volatile boolean starting;

    private ScheduledExecutorService scheduledExecutorService;

    //启动阶段
    @Override
    public void starting() {
        starting = true;
    }

    //正在启动中
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        if (EnvUtil.getStandaloneMode()) {
            scheduledExecutorService = new ScheduledThreadPoolExecutor(
                    1,
                    r -> {
                        Thread thread = new Thread(r);
                        thread.setName("server.hippo4j-starting");
                        return thread;
                    });
            scheduledExecutorService.scheduleWithFixedDelay(() -> {
                if (starting) {
                    log.info("Hippo4J is starting...");
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    //启动成功
    @Override
    public void started(ConfigurableApplicationContext context) {
        starting = false;
        closeExecutor();
        if (EnvUtil.getStandaloneMode()) {
            log.info("Hippo4J started successfully...");
        }
    }

    //启动失败
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        log.error("Startup errors: {}", exception);
        closeExecutor();
        context.close();
        log.error("Hippo4J failed to start, please see {} for more details.",
                Paths.get(EnvUtil.getHippo4JHome(), "logs/hippo4j.log"));
    }


    private void closeExecutor() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
    }
}
