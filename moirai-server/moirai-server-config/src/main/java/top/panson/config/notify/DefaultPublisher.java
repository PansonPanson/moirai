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
import top.panson.config.notify.listener.AbstractSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/7
 * @方法描述：默认的事件发布器，在这个框架中，整个事件通知机制都是复制了nacos的那一套，代码也都几乎一样，其实作者没必要这么搞
 * 应该就是不想自己写了，单纯省事，所以直接把nacos的那一套搬过来了，但是在整个框架中，这个事件通知机制也就长轮询用到了，未免有点小题大做
 */
@Slf4j
public class DefaultPublisher extends Thread implements EventPublisher {

    //存放观察者的集合
    protected final Set<AbstractSubscriber> subscribers = Collections.synchronizedSet(new HashSet<>());

    private BlockingQueue<AbstractEvent> queue;

    //这个事件发布器继承了一个线程，实际上它就是一个线程，所以要弄一个初始化状态，线程一旦启动，就把这个标志设置为true
    //代表线程已经启动了，并且只能启动一次
    private volatile boolean initialized = false;

    //线程是否停止工作
    private volatile boolean shutdown = false;

    //队列最大容量
    private int queueMaxSize = -1;

    //被处理过的最新事件的序号
    protected volatile Long lastEventSequence = -1L;

    //原子更新器，用来更新队列中最后一个事件序号
    private static final AtomicReferenceFieldUpdater<DefaultPublisher, Long> UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(DefaultPublisher.class, Long.class, "lastEventSequence");

    //初始化方法
    @Override
    public void init(Class<? extends AbstractEvent> type, int bufferSize) {
        //设置守护线程
        setDaemon(true);
        //设置线程名称
        setName("dynamic.thread-pool.publisher-" + type.getName());
        //设置队列最大容量
        this.queueMaxSize = bufferSize;
        //创建事件队列
        this.queue = new ArrayBlockingQueue(bufferSize);
        //在这里启动了线程
        start();
    }


    //启动线程
    @Override
    public synchronized void start() {
        if (!initialized) {
            super.start();
            if (queueMaxSize == -1) {
                queueMaxSize = NotifyCenter.ringBufferSize;
            }
            initialized = true;
        }
    }


    //当前线程要执行的方法
    @Override
    public void run() {
        //处理事件队列中的事件
        openEventHandler();
    }

    private void openEventHandler() {
        try {
            //这里的设定比较细节，请大家想一想，假如有事件发布了，但是没有订阅者怎么办？那这个事件就直接丢掉吗？
            //直接丢掉显然不合适，但是事件发布了很久，一直没有订阅者，就可以丢掉了，所有这里定义了一个等待时间
            int waitTimes = 60;
            for (;;) {//判断线程是否停止工作了，或者是不是有订阅者了，或者等待时间小于0了
                if (shutdown || hasSubscriber() || waitTimes <= 0) {
                    //这几个条件满足一个就可以退出循环了
                    break;
                }
                try {//如果上面三个条件都不满足，就让当前线程睡一会，等待订阅者订阅事件
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }//如果等待时间减到0了，不管有没有订阅者，都要处理事件队列中的事件了
                waitTimes--;
            }//下面就是处理事件队列中事件的操作
            for (;;) {
                if (shutdown) {
                    break;
                }//这里使用阻塞获取事件的方法来获取事件，也就意味着假如现在发布器中有订阅者订阅了事件，但是一直没有事件发布
                //那么当前线程也会阻塞，直到有事件发布
                final AbstractEvent event = queue.take();
                //处理事件
                receiveEvent(event);
                //更新被处理过的最新事件的序号
                UPDATER.compareAndSet(this, lastEventSequence, Math.max(lastEventSequence, event.sequence()));
            }
        } catch (Throwable ex) {
            log.error("Event listener exception.", ex);
        }
    }

    //添加订阅者的方法
    @Override
    public void addSubscriber(AbstractSubscriber subscriber) {
        subscribers.add(subscriber);
    }


    //发布事件的方法，这个方法并不是当前线程自己执行的，而是外部线程调用的
    @Override
    public boolean publish(AbstractEvent event) {
        //把事件放到事件队列中
        boolean success = this.queue.offer(event);
        //添加失败则直接处理事件
        if (!success) {
            log.warn("Unable to plug in due to interruption, synchronize sending time, event: {}", event);
            //这里直接处理时间的话，就是外部线程在处理事件
            receiveEvent(event);
            return true;
        }
        return true;
    }

    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：本类核心方法，通知订阅者执行回调方法
     */
    @Override
    public void notifySubscriber(AbstractSubscriber subscriber, AbstractEvent event) {
        //创建一个任务，任务逻辑就是执行订阅者的回调方法
        final Runnable job = () -> subscriber.onEvent(event);
        //判断当前订阅者是否自定义了执行器
        final Executor executor = subscriber.executor();
        //如果定义了就使用订阅者自己的执行器执行回调方法
        if (executor != null) {
            executor.execute(job);
        } else {
            try {//没定义就是用当前线程执行回调方法
                job.run();
            } catch (Throwable e) {
                log.error("Event callback exception: {}", e);
            }
        }
    }

    //判断是否有订阅者的方法
    private boolean hasSubscriber() {
        return !CollectionUtils.isEmpty(subscribers);
    }

    void receiveEvent(AbstractEvent event) {
        //遍历订阅者
        for (AbstractSubscriber subscriber : subscribers) {
            //通知订阅者执行回调方法
            notifySubscriber(subscriber, event);
        }
    }
}
