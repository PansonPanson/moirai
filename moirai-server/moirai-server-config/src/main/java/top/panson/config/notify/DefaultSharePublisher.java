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

package top.panson.config.notify;

import top.panson.config.event.AbstractEvent;
import top.panson.config.event.AbstractSlowEvent;
import top.panson.config.notify.listener.AbstractSubscriber;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** 
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。 
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：这个是默认的共享事件发布器，这个类的逻辑非常简单，所谓共享，就是这个发布器可以发布很多事件，订阅不同事件的订阅者都可以存放到这个发布器中，这就是共享发布器的意思
 * 这个共享时间发布器主要处理慢事件类型的事件。但是在当前框架中，这个发布器根本用不到，因为是直接从nacos中搬运过来的，这个类在nacos中作用很大
 */ 
public class DefaultSharePublisher extends DefaultPublisher {

    private final Map<Class<? extends AbstractSlowEvent>, Set<AbstractSubscriber>> subMappings = new ConcurrentHashMap();

    protected final Set<AbstractSubscriber> subscribers = Collections.synchronizedSet(new HashSet<>());

    private final Lock lock = new ReentrantLock();

    public void addSubscriber(AbstractSubscriber subscriber, Class<? extends AbstractEvent> subscribeType) {
        Class<? extends AbstractSlowEvent> subSlowEventType = (Class<? extends AbstractSlowEvent>) subscribeType;
        subscribers.add(subscriber);
        lock.lock();
        try {
            Set<AbstractSubscriber> sets = subMappings.get(subSlowEventType);
            if (sets == null) {
                Set<AbstractSubscriber> newSet = Collections.synchronizedSet(new HashSet<>());
                newSet.add(subscriber);
                subMappings.put(subSlowEventType, newSet);
                return;
            }
            sets.add(subscriber);
        } finally {
            lock.unlock();
        }
    }
}
