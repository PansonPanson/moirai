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

import top.panson.common.config.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Executor;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/9
 * @方法描述：web容器线程池处理器的抽象父类，这个父类实现了springboot的ApplicationRunner接口，springboot容器启动完毕之后，这个接口中的方法才会被回调
 */
@Slf4j
public abstract class AbstractWebThreadPoolService implements WebThreadPoolService, ApplicationRunner {

    /**
     * Thread pool executor
     */
    protected volatile Executor executor;

    /**
     * Get web thread pool by server
     *
     * @param webServer
     * @return
     */
    protected abstract Executor getWebThreadPoolByServer(WebServer webServer);


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/9
     * @方法描述：得到web容器线程池的方法
     */
    @Override
    public Executor getWebThreadPool() {
        if (executor == null) {
            synchronized (AbstractWebThreadPoolService.class) {
                if (executor == null) {
                    //调用子类的方法得到web容器线程池
                    executor = getWebThreadPoolByServer(getWebServer());
                }
            }
        }
        return executor;
    }


    @Override
    public WebServer getWebServer() {
        ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
        WebServer webServer = ((WebServerApplicationContext) applicationContext).getWebServer();
        return webServer;
    }


    //在这里回调了ApplicationRunner接口中的方法
    @Override
    public void run(ApplicationArguments args) {
        try {//在这里得到了web容器的线程池
            getWebThreadPool();
        } catch (Exception ignored) {
        }
    }
}
