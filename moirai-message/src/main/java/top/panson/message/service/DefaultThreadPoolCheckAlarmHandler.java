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

package top.panson.message.service;

import top.panson.common.api.ThreadPoolCheckAlarm;
import top.panson.common.toolkit.CalculateUtil;
import top.panson.common.toolkit.StringUtil;
import top.panson.core.executor.DynamicThreadPoolExecutor;
import top.panson.core.executor.DynamicThreadPoolWrapper;
import top.panson.core.executor.manage.GlobalThreadPoolManage;
import top.panson.core.executor.support.ThreadPoolBuilder;
import top.panson.core.toolkit.ExecutorTraceContextUtil;
import top.panson.core.toolkit.IdentifyUtil;
import top.panson.message.enums.NotifyTypeEnum;
import top.panson.message.request.AlarmNotifyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 *
 * @方法描述：默认的给用户发送线程池告警信息的对象，其中ThreadPoolCheckAlarm接口继承了CommandLineRunner接口
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultThreadPoolCheckAlarmHandler implements Runnable, ThreadPoolCheckAlarm {

    private final Hippo4jSendMessageService hippo4jSendMessageService;

    @Value("${spring.profiles.active:UNKNOWN}")
    private String active;

    @Value("${spring.dynamic.thread-pool.item-id:}")
    private String itemId;

    @Value("${spring.application.name:UNKNOWN}")
    private String applicationName;

    //检查是否应该给用户发送告警通知信息的频率
    @Value("${spring.dynamic.thread-pool.check-state-interval:5}")
    private Integer checkStateInterval;


    //创建一个定时任务执行器，这个执行器执行的就是定期检查客户端给是否应该用户平台发送告警通知的任务
    private final ScheduledExecutorService ALARM_NOTIFY_EXECUTOR = new ScheduledThreadPoolExecutor(
            1,
            r -> new Thread(r, "client.alarm.notify"));


    //这个线程池执行的就是给用户发送告警通知的任务
    private final ExecutorService ASYNC_ALARM_NOTIFY_EXECUTOR = ThreadPoolBuilder.builder()
            .poolThreadSize(2, 4)
            .threadFactory("client.execute.timeout.alarm")
            .allowCoreThreadTimeOut(true)
            .keepAliveTime(60L, TimeUnit.SECONDS)
            .workQueue(new LinkedBlockingQueue(4096))
            .rejected(new ThreadPoolExecutor.AbortPolicy())
            .build();


    //这个是CommandLineRunner接口中的回调方法
    @Override
    public void run(String... args) throws Exception {
        //该方法执行的时候，会向定时任务执行其中提交定时任务，定时任务执行的频率就是checkStateInterval成员变量的值
        ALARM_NOTIFY_EXECUTOR.scheduleWithFixedDelay(this, 0, checkStateInterval, TimeUnit.SECONDS);
    }

    /**
     * @方法描述：这个就是定时任务执行器要定期执行的任务
     */
    @Override
    public void run() {
        //得到客户端所有线程池的Id
        List<String> listThreadPoolId = GlobalThreadPoolManage.listThreadPoolId();
        //遍历线程池Id
        listThreadPoolId.forEach(threadPoolId -> {
            ThreadPoolNotifyAlarm threadPoolNotifyAlarm = GlobalNotifyAlarmManage.get(threadPoolId);
            if (threadPoolNotifyAlarm != null && threadPoolNotifyAlarm.getAlarm()) {
                DynamicThreadPoolWrapper wrapper = GlobalThreadPoolManage.getExecutorService(threadPoolId);
                ThreadPoolExecutor executor = wrapper.getExecutor();
                //检查线程池的队列是否需要报警
                checkPoolCapacityAlarm(threadPoolId, executor);
                //检查线程池活跃度是否需要报警
                checkPoolActivityAlarm(threadPoolId, executor);
            }
        });
    }


    /**
     * @方法描述：检查线程池队列容量是否需要报警的方法
     */
    @Override
    public void checkPoolCapacityAlarm(String threadPoolId, ThreadPoolExecutor threadPoolExecutor) {
        ThreadPoolNotifyAlarm alarmConfig = GlobalNotifyAlarmManage.get(threadPoolId);
        if (Objects.isNull(alarmConfig) || !alarmConfig.getAlarm() || alarmConfig.getCapacityAlarm() <= 0) {
            return;
        }
        BlockingQueue blockingQueue = threadPoolExecutor.getQueue();
        //得到队列中当前使用容量
        int queueSize = blockingQueue.size();
        //得到队列剩余容量
        int capacity = queueSize + blockingQueue.remainingCapacity();
        //得到队列容量使用率
        int divide = CalculateUtil.divide(queueSize, capacity);
        //判断队列容量使用率是否超过了告警阈值，alarmConfig.getCapacityAlarm()这里得到的就是告警阈值，可以从registerNotifyAlarm方法中查看阈值是怎么设置的
        //至于registerNotifyAlarm方法，会在DynamicThreadPoolPostProcessor、DynamicThreadPoolConfigService这两个类中被调用
        boolean isSend = alarmConfig.getAlarm() && divide > alarmConfig.getCapacityAlarm();
        if (isSend) {
            //超过了告警阈值就直接给用户发送告警通知
            AlarmNotifyRequest alarmNotifyRequest = buildAlarmNotifyRequest(threadPoolExecutor);
            alarmNotifyRequest.setThreadPoolId(threadPoolId);
            //发送的是队列容量告警通知
            hippo4jSendMessageService.sendAlarmMessage(NotifyTypeEnum.CAPACITY, alarmNotifyRequest);
        }
    }


    /**
     * @方法描述：检查线程池活跃度是否需要报警的方法
     */
    @Override
    public void checkPoolActivityAlarm(String threadPoolId, ThreadPoolExecutor threadPoolExecutor) {
        ThreadPoolNotifyAlarm alarmConfig = GlobalNotifyAlarmManage.get(threadPoolId);
        if (Objects.isNull(alarmConfig) || !alarmConfig.getAlarm() || alarmConfig.getActiveAlarm() <= 0) {
            return;
        }//得到线程池当前活跃线程
        int activeCount = threadPoolExecutor.getActiveCount();
        //得到最大线程
        int maximumPoolSize = threadPoolExecutor.getMaximumPoolSize();
        //得到线程池负载率
        int divide = CalculateUtil.divide(activeCount, maximumPoolSize);
        //判断是否超过了阈值
        boolean isSend = alarmConfig.getAlarm() && divide > alarmConfig.getActiveAlarm();
        if (isSend) {
            AlarmNotifyRequest alarmNotifyRequest = buildAlarmNotifyRequest(threadPoolExecutor);
            alarmNotifyRequest.setThreadPoolId(threadPoolId);
            //超过了就发送告警通知，通知类型为线程活跃告警通知
            hippo4jSendMessageService.sendAlarmMessage(NotifyTypeEnum.ACTIVITY, alarmNotifyRequest);
        }
    }


    /**
     * @方法描述：异步给线程池发送报警信息的方法，这个方法会在TaskRejectNotifyAlarmPlugin类中被调用，每当线程池拒绝了某个任务，就会调用这个方法通知用户
     */
    @Override
    public void asyncSendRejectedAlarm(String threadPoolId) {
        Runnable checkPoolRejectedAlarmTask = () -> {
            ThreadPoolNotifyAlarm alarmConfig = GlobalNotifyAlarmManage.get(threadPoolId);
            if (Objects.isNull(alarmConfig) || !alarmConfig.getAlarm()) {
                return;
            }
            ThreadPoolExecutor threadPoolExecutor = GlobalThreadPoolManage.getExecutorService(threadPoolId).getExecutor();
            if (threadPoolExecutor instanceof DynamicThreadPoolExecutor) {
                AlarmNotifyRequest alarmNotifyRequest = buildAlarmNotifyRequest(threadPoolExecutor);
                alarmNotifyRequest.setThreadPoolId(threadPoolId);
                hippo4jSendMessageService.sendAlarmMessage(NotifyTypeEnum.REJECT, alarmNotifyRequest);
            }
        };//在这里把任务提交给ASYNC_ALARM_NOTIFY_EXECUTOR线程池
        ASYNC_ALARM_NOTIFY_EXECUTOR.execute(checkPoolRejectedAlarmTask);
    }


    /**
     * @方法描述：异步给用户发送线程池中执行任务超时的告警信息的方法
     */
    @Override
    public void asyncSendExecuteTimeOutAlarm(String threadPoolId, long executeTime, long executeTimeOut, ThreadPoolExecutor threadPoolExecutor) {
        ThreadPoolNotifyAlarm alarmConfig = GlobalNotifyAlarmManage.get(threadPoolId);
        if (Objects.isNull(alarmConfig) || !alarmConfig.getAlarm()) {
            return;
        }
        if (threadPoolExecutor instanceof DynamicThreadPoolExecutor) {
            try {
                AlarmNotifyRequest alarmNotifyRequest = buildAlarmNotifyRequest(threadPoolExecutor);
                alarmNotifyRequest.setThreadPoolId(threadPoolId);
                //设置线程执行本次任务耗费的时间，这个时间肯定大于超时时间，否则不会触发告警通知
                alarmNotifyRequest.setExecuteTime(executeTime);
                //设置任务执行的超时时间
                alarmNotifyRequest.setExecuteTimeOut(executeTimeOut);
                //移除线程本地map中的任务超时标志
                String executeTimeoutTrace = ExecutorTraceContextUtil.getAndRemoveTimeoutTrace();
                //把标志设置到请求中，告诉用户
                if (StringUtil.isNotBlank(executeTimeoutTrace)) {
                    alarmNotifyRequest.setExecuteTimeoutTrace(executeTimeoutTrace);
                }
                //创建发送通知的异步任务
                Runnable task = () -> hippo4jSendMessageService.sendAlarmMessage(NotifyTypeEnum.TIMEOUT, alarmNotifyRequest);
                //异步发送告警通知
                ASYNC_ALARM_NOTIFY_EXECUTOR.execute(task);
            } catch (Throwable ex) {
                // 如果在执行过程中出现异常，记录错误日志
                log.error("Send thread pool execution timeout alarm error.", ex);
            }
        }
    }



    public AlarmNotifyRequest buildAlarmNotifyRequest(ThreadPoolExecutor threadPoolExecutor) {
        BlockingQueue<Runnable> blockingQueue = threadPoolExecutor.getQueue();
        RejectedExecutionHandler rejectedExecutionHandler = threadPoolExecutor.getRejectedExecutionHandler();
        long rejectCount = threadPoolExecutor instanceof DynamicThreadPoolExecutor
                ? ((DynamicThreadPoolExecutor) threadPoolExecutor).getRejectCountNum()
                : -1L;
        return AlarmNotifyRequest.builder()
                .appName(StringUtil.isBlank(itemId) ? applicationName : itemId)
                .active(active.toUpperCase())
                .identify(IdentifyUtil.getIdentify())
                .corePoolSize(threadPoolExecutor.getCorePoolSize())
                .maximumPoolSize(threadPoolExecutor.getMaximumPoolSize())
                .poolSize(threadPoolExecutor.getPoolSize())
                .activeCount(threadPoolExecutor.getActiveCount())
                .largestPoolSize(threadPoolExecutor.getLargestPoolSize())
                .completedTaskCount(threadPoolExecutor.getCompletedTaskCount())
                .queueName(blockingQueue.getClass().getSimpleName())
                .capacity(blockingQueue.size() + blockingQueue.remainingCapacity())
                .queueSize(blockingQueue.size())
                .remainingCapacity(blockingQueue.remainingCapacity())
                .rejectedExecutionHandlerName(rejectedExecutionHandler.getClass().getSimpleName())
                .rejectCountNum(rejectCount)
                .build();
    }
}
