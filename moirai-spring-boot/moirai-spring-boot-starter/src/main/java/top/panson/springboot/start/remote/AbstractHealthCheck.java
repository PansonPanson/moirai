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

package top.panson.springboot.start.remote;

import top.panson.common.design.builder.ThreadFactoryBuilder;
import top.panson.common.toolkit.ThreadUtil;
import top.panson.springboot.start.core.ShutdownExecuteException;
import top.panson.springboot.start.event.ApplicationRefreshedEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;

import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static top.panson.common.constant.Constants.HEALTH_CHECK_INTERVAL;



/**
 * @方法描述：健康检查抽象类
 */
@Slf4j
public abstract class AbstractHealthCheck implements ServerHealthCheck, InitializingBean, ApplicationListener<ApplicationRefreshedEvent> {


    //当前服务端的健康状况，默认是健康状态
    private volatile boolean healthStatus = true;

    //健康检查失败次数
    private volatile int checkFailureCount = 0;

    //客户端关闭的标志，这个标志会在jvm关闭时的钩子函数中被设置为true
    //默认初始值为false，也就是客户端程序没有关闭
    private volatile boolean clientShutdownHook = false;

    //Springboot容器是否初始化完毕
    private boolean contextInitComplete = false;

    //同步锁
    private final ReentrantLock healthMainLock = new ReentrantLock();

    //健康等待条件
    private final Condition healthCondition = healthMainLock.newCondition();

    //定期执行健康检测任务的执行器
    private final ScheduledThreadPoolExecutor healthCheckExecutor = new ScheduledThreadPoolExecutor(
            //只有一个线程
            new Integer(1),
            ThreadFactoryBuilder.builder().daemon(true).prefix("client.scheduled.health.check").build());


    /**
     * @方法描述：向服务端发送健康检查请求的抽象方法，由子类实现
     */
    protected abstract boolean sendHealthCheck();



    /**
     * @方法描述：执行健康检查的入口方法
     */
    public void healthCheck() {
        //向服务端发送健康检查请求，得到服务端健康状态
        boolean healthCheckStatus = sendHealthCheck();
        if (healthCheckStatus) {
            //走到这里意味着服务端健康没有问题
            if (Objects.equals(healthStatus, false)) {
                //把健康状态更新为true即可，当然，如果客户端的healthStatus本身就是true
                //就不会进入这个if分支
                healthStatus = true;
                //健康检查失败次数重置为0
                checkFailureCount = 0;
                log.info("The client reconnects to the server successfully.");
                //唤醒在healthCondition等待的线程
                signalAllBizThread();
            }
        } else {
            //走动这里意味着服务端状态并不健康
            //把客户端的healthStatus更新为false
            healthStatus = false;
            //自增健康检查失败次数
            checkFailureCount++;
            //然后根据健康检查失败次数让执行健康检查的线程沉睡一会
            if (checkFailureCount > 1 && checkFailureCount < 4) {
                ThreadUtil.sleep(HEALTH_CHECK_INTERVAL * 1000 * (checkFailureCount - 1));
            } else if (checkFailureCount >= 4) {
                ThreadUtil.sleep(25000L);
            }
        }
    }


    /**
     * @方法描述：判断服务端是否健康的方法
     */
    @Override
    @SneakyThrows
    public boolean isHealthStatus() {
        //首先判断springboot容器是否完全初始化了，如果健康状态为false，并且客户端程序还没有关闭
        //这时候就要重新去检查服务端的健康状态，如果healthStatus为true，那么根本就不会进入这个while循环
        //直接就会把healthStatus的状态返回
        while (contextInitComplete && !healthStatus && !clientShutdownHook) {
            //获得同步锁
            healthMainLock.lock();
            try {//让当前执行isHealthStatus方法的线程等待，注意，这里是让执行isHealthStatus方法的线程等待
                //大家可以看看isHealthStatus方法会在哪里被调用就清楚是怎么回事了
                //这里我要给大家解释一下，为什么要这么做，因为只要进入了这个if分支，就意味着当前客户端检查服务端的状态是失败的
                //显然，客户端就没办法向服务端发送各种请求处理业务，只有当服务端恢复健康状态之后，客户端才能继续发送请求
                //所以这里要让调用isHealthStatus方法的线程阻塞一会，直到服务端恢复健康状态了，才会唤醒healthCondition上阻塞的所有线程
                //让程序继续执行下去，那么怎么就知道服务端恢复健康状态了呢？这就是当前类对象的healthCheckExecutor定时任务执行器的工作了
                //这个定时任务执行器会定期检查服务端健康状态，一旦服务端恢复健康了，就会在healthCheck方法中调用signalAllBizThread方法
                //在signalAllBizThread方法中，就会唤醒所有阻塞在healthCondition上的线程，这样一来，程序就可以继续执行了
                healthCondition.await();
            } finally {
                //最后释放同步锁
                healthMainLock.unlock();
            }
        }
        if (!healthStatus) {
            throw new ShutdownExecuteException();
        }
        return healthStatus;
    }


    //设置健康状态的方法
    @Override
    public void setHealthStatus(boolean healthStatus) {
        healthMainLock.lock();
        try {
            this.healthStatus = healthStatus;
            log.warn("The server health status setting is unavailable.");
        } finally {
            healthMainLock.unlock();
        }
    }


    //唤醒所有在healthCondition等待的线程
    private void signalAllBizThread() {
        healthMainLock.lock();
        try {
            healthCondition.signalAll();
        } finally {
            healthMainLock.unlock();
        }
    }


    /**
     * @方法描述：这个方法是InitializingBean接口中的扩展方法，该方法会在当前类的bean对象初始化完毕之后调用
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //在这里向jvm添加了一个钩子函数，jvm关闭的时候执行这个函数
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //首先把客户端关闭状态设置为true
            clientShutdownHook = true;
            //然后唤醒所有在healthCondition上等待的线程
            signalAllBizThread();
        }));
        //这里就不是钩子函数中的内容了，这里就是在bean初始化完毕之后，启动定时任务执行器，开始定时执行服务端的健康检查操作
        healthCheckExecutor.scheduleWithFixedDelay(this::healthCheck, 0, HEALTH_CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    //该方法会在ApplicationRefreshedEvent事件发布之后被回调，具体逻辑请看ApplicationContentPostProcessor类中的注释
    @Override
    public void onApplicationEvent(ApplicationRefreshedEvent event) {
        //springboot容器初始化完毕之后，就会把contextInitComplete设置为true
        contextInitComplete = true;
    }
}
