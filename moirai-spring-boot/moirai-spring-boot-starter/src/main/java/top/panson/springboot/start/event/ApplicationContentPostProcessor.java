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

package top.panson.springboot.start.event;

import top.panson.springboot.start.core.ClientWorker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/6
 * @方法描述：在这个类中，要想知道ApplicationRefreshedEvent事件的发布时机，首先应该知道ContextRefreshedEvent事件的发布时机
 * ContextRefreshedEvent在springboot中是一个容器刷新事件，也就是说当springboot容器的所有bean都被创建并且初始化完毕之后，这个事件就会被发布
 * 这个ContextRefreshedEvent事件是由springboot发布的，发布之后就会调用ApplicationContentPostProcessor这个监听器中的onApplicationEvent方法
 * 然后就会让springboot再次发布一个用户自己定义的ApplicationRefreshedEvent事件，紧接着AbstractHealthCheck这个监听器就会执行它内部的onApplicationEvent方法了
 */
public class ApplicationContentPostProcessor implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private ClientWorker clientWorker;

    private final AtomicBoolean executeOnlyOnce = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //只执行一次
        if (!executeOnlyOnce.compareAndSet(false, true)) {
            return;
        }
        applicationContext.publishEvent(new ApplicationRefreshedEvent(this));
        clientWorker.notifyApplicationComplete();
    }
}
