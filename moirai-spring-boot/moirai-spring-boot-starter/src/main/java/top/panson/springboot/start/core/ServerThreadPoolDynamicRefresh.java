package top.panson.springboot.start.core;

import top.panson.common.api.ThreadPoolConfigChange;
import top.panson.common.api.ThreadPoolDynamicRefresh;
import top.panson.common.enums.EnableEnum;
import top.panson.common.executor.support.BlockingQueueTypeEnum;
import top.panson.common.executor.support.RejectedPolicyTypeEnum;
import top.panson.common.executor.support.ResizableCapacityLinkedBlockingQueue;
import top.panson.common.model.ThreadPoolParameter;
import top.panson.common.model.ThreadPoolParameterInfo;
import top.panson.common.toolkit.JSONUtil;
import top.panson.core.executor.DynamicThreadPoolExecutor;
import top.panson.core.executor.manage.GlobalThreadPoolManage;
import top.panson.message.request.ChangeParameterNotifyRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static top.panson.common.constant.ChangeThreadPoolConstants.CHANGE_DELIMITER;
import static top.panson.common.constant.ChangeThreadPoolConstants.CHANGE_THREAD_POOL_TEXT;

/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/7
 * @方法描述：线程池配置信息动态刷新器，这个动态刷新器在第三版本还不完善，因为在动态刷新本地线程池信息后，实际上还要通知用户
 * 但是在第三版本代码中，还没有实现通知用户的功能，后面会重构完整，主要是重构refreshDynamicPool方法
 */
@Slf4j
@AllArgsConstructor
public class ServerThreadPoolDynamicRefresh implements ThreadPoolDynamicRefresh {

    private final ThreadPoolConfigChange threadPoolConfigChange;


    //动态刷新本地线程池的入口方法
    @Override
    public void dynamicRefresh(String content) {
        //把从服务端得到的最新的线程池的配置信息转换为ThreadPoolParameterInfo对象
        ThreadPoolParameterInfo parameter = JSONUtil.parseObject(content, ThreadPoolParameterInfo.class);
        //得到线程池Id
        String threadPoolId = parameter.getTpId();
        //从全局线程池管理器中得到对应的线程池，这个线程池就是程序中正在运行的线程池
        ThreadPoolExecutor executor = GlobalThreadPoolManage.getExecutorService(threadPoolId).getExecutor();
        //刷新线程池的信息
        refreshDynamicPool(parameter, executor);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：动态刷新线程池信息的方法，这个方法在第三版本并不完善，我省略了很多代码，都是和通知用户线程池信息发生变更的操作
     * 后面会重构完整
     */
    private void refreshDynamicPool(ThreadPoolParameter parameter, ThreadPoolExecutor executor) {
        //下面先得到更新前的线程池信息
        String threadPoolId = parameter.getTpId();
        int originalCoreSize = executor.getCorePoolSize();
        int originalMaximumPoolSize = executor.getMaximumPoolSize();
        String originalQuery = executor.getQueue().getClass().getSimpleName();
        int originalCapacity = executor.getQueue().remainingCapacity() + executor.getQueue().size();
        long originalKeepAliveTime = executor.getKeepAliveTime(TimeUnit.SECONDS);
        boolean originalAllowCoreThreadTimeOut = executor.allowsCoreThreadTimeOut();
        Long originalExecuteTimeOut = null;
        RejectedExecutionHandler rejectedExecutionHandler = executor.getRejectedExecutionHandler();
        if (executor instanceof DynamicThreadPoolExecutor) {
            DynamicThreadPoolExecutor dynamicExecutor = (DynamicThreadPoolExecutor) executor;
            rejectedExecutionHandler = dynamicExecutor.getRejectedExecutionHandler();
            originalExecuteTimeOut = dynamicExecutor.getExecuteTimeOut();
        }
        //在这里刷新线程池信息
        changePoolInfo(executor, parameter);
        //得到更新后的线程池
        ThreadPoolExecutor afterExecutor = GlobalThreadPoolManage.getExecutorService(threadPoolId).getExecutor();
        String originalRejected = rejectedExecutionHandler.getClass().getSimpleName();
        Long executeTimeOut = Optional.ofNullable(parameter.getExecuteTimeOut()).orElse(0L);
        //线程池的配置动态刷新之后，要发送通知给用户
        ChangeParameterNotifyRequest changeNotifyRequest = ChangeParameterNotifyRequest.builder()
                .beforeCorePoolSize(originalCoreSize)
                .beforeMaximumPoolSize(originalMaximumPoolSize)
                .beforeAllowsCoreThreadTimeOut(originalAllowCoreThreadTimeOut)
                .beforeKeepAliveTime(originalKeepAliveTime)
                .blockingQueueName(originalQuery)
                .beforeQueueCapacity(originalCapacity)
                .beforeRejectedName(originalRejected)
                .beforeExecuteTimeOut(originalExecuteTimeOut)
                .nowCorePoolSize(afterExecutor.getCorePoolSize())
                .nowMaximumPoolSize(afterExecutor.getMaximumPoolSize())
                .nowAllowsCoreThreadTimeOut(EnableEnum.getBool(parameter.getAllowCoreThreadTimeOut()))
                .nowKeepAliveTime(afterExecutor.getKeepAliveTime(TimeUnit.SECONDS))
                .nowQueueCapacity((afterExecutor.getQueue().remainingCapacity() + afterExecutor.getQueue().size()))
                .nowRejectedName(RejectedPolicyTypeEnum.getRejectedNameByType(parameter.getRejectedType()))
                .nowExecuteTimeOut(executeTimeOut)
                .build();
        changeNotifyRequest.setThreadPoolId(threadPoolId);
        threadPoolConfigChange.sendPoolConfigChange(changeNotifyRequest);
        //记录日志，并且在控制台输出，更新前和更新后做一下对比
        log.info(CHANGE_THREAD_POOL_TEXT,
                threadPoolId,
                String.format(CHANGE_DELIMITER, originalCoreSize, afterExecutor.getCorePoolSize()),
                String.format(CHANGE_DELIMITER, originalMaximumPoolSize, afterExecutor.getMaximumPoolSize()),
                String.format(CHANGE_DELIMITER, originalCapacity, (afterExecutor.getQueue().remainingCapacity() + afterExecutor.getQueue().size())),
                String.format(CHANGE_DELIMITER, originalKeepAliveTime, afterExecutor.getKeepAliveTime(TimeUnit.SECONDS)),
                String.format(CHANGE_DELIMITER, originalExecuteTimeOut, executeTimeOut),
                String.format(CHANGE_DELIMITER, originalRejected, RejectedPolicyTypeEnum.getRejectedNameByType(parameter.getRejectedType())),
                String.format(CHANGE_DELIMITER, originalAllowCoreThreadTimeOut, EnableEnum.getBool(parameter.getAllowCoreThreadTimeOut())));
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：刷线客户端本地线程池信息的方法
     */
    private void changePoolInfo(ThreadPoolExecutor executor, ThreadPoolParameter parameter) {
        //判断一下服务端更新之后的线程池信息是否设置了线程池的核心线程数和最大线程数
        if (parameter.getCoreSize() != null && parameter.getMaxSize() != null) {
            //如果服务端更新之后的最大线程数小于当前客户端线程池的最大线程数
            if (parameter.getMaxSize() < executor.getMaximumPoolSize()) {
                //那就先更新核心线程数，防止最大线程数小于本地线程池的核心线程数
                executor.setCorePoolSize(parameter.getCoreSize());
                //然后再更新本地现成的最大线程数
                executor.setMaximumPoolSize(parameter.getMaxSize());
            } else {
                //走到这里就意味着服务端的最大线程数大于本地的最大线程数
                //那就可以先更新最大线程数，然后再更新核心线程数
                executor.setMaximumPoolSize(parameter.getMaxSize());
                executor.setCorePoolSize(parameter.getCoreSize());
            }
        } else {
            if (parameter.getMaxSize() != null) {
                executor.setMaximumPoolSize(parameter.getMaxSize());
            }
            if (parameter.getCoreSize() != null) {
                executor.setCorePoolSize(parameter.getCoreSize());
            }
        }//判断队列容量做非空判断，同时判断队列类型是否为ResizableCapacityLinkedBlockingQueue类型
        if (parameter.getCapacity() != null
                && Objects.equals(BlockingQueueTypeEnum.RESIZABLE_LINKED_BLOCKING_QUEUE.getType(), parameter.getQueueType())) {
            if (executor.getQueue() instanceof ResizableCapacityLinkedBlockingQueue) {
                ResizableCapacityLinkedBlockingQueue queue = (ResizableCapacityLinkedBlockingQueue) executor.getQueue();
                queue.setCapacity(parameter.getCapacity());
            } else {
                log.warn("The queue length cannot be modified. Queue type mismatch. Current queue type: {}", executor.getQueue().getClass().getSimpleName());
            }
        }//更新线程存活时间
        if (parameter.getKeepAliveTime() != null) {
            executor.setKeepAliveTime(parameter.getKeepAliveTime(), TimeUnit.SECONDS);
        }//更新任务执行超时时间
        Long executeTimeOut = Optional.ofNullable(parameter.getExecuteTimeOut()).orElse(0L);
        if (executor instanceof DynamicThreadPoolExecutor) {
            ((DynamicThreadPoolExecutor) executor).setExecuteTimeOut(executeTimeOut);
        }//更新拒绝策略
        if (parameter.getRejectedType() != null) {
            RejectedExecutionHandler rejectedExecutionHandler = RejectedPolicyTypeEnum.createPolicy(parameter.getRejectedType());
            executor.setRejectedExecutionHandler(rejectedExecutionHandler);
        }//更新是否允许超过存活时间的核心线程终止工作
        if (parameter.getAllowCoreThreadTimeOut() != null) {
            executor.allowCoreThreadTimeOut(EnableEnum.getBool(parameter.getAllowCoreThreadTimeOut()));
        }
    }
}
