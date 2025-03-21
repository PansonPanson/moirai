package top.panson.core.executor.state;

import top.panson.common.model.ThreadPoolRunStateInfo;
import top.panson.common.toolkit.CalculateUtil;
import top.panson.core.executor.DynamicThreadPoolExecutor;
import top.panson.core.executor.DynamicThreadPoolWrapper;
import top.panson.core.executor.manage.GlobalThreadPoolManage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：收集线程池运行信息收集器的父类
 */
public abstract class AbstractThreadPoolRuntime {



    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：这是一个抽象方法，用来得到线程池运行的补充信息
     */
    public abstract ThreadPoolRunStateInfo supplement(ThreadPoolRunStateInfo threadPoolRunStateInfo);


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：根据线程池Id获得对应动态线程池的运行信息
     */
    public ThreadPoolRunStateInfo getPoolRunState(String threadPoolId) {
        //从全局线程池管理器中得到被包装的线程池对象
        DynamicThreadPoolWrapper executorService = GlobalThreadPoolManage.getExecutorService(threadPoolId);
        //得到真正运行的线程池本身
        ThreadPoolExecutor pool = executorService.getExecutor();
        //收集线程池运行信息
        return getPoolRunState(threadPoolId, pool);
    }



    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：收集线程池运行信息的方法
     */
    public ThreadPoolRunStateInfo getPoolRunState(String threadPoolId, Executor executor) {
        //得到真正执行任务的线程池
        ThreadPoolExecutor actualExecutor = (ThreadPoolExecutor) executor;
        //得到活跃线程数量
        int activeCount = actualExecutor.getActiveCount();
        //得到曾经创建的最大线程数量
        int largestPoolSize = actualExecutor.getLargestPoolSize();
        //得到任务队列
        BlockingQueue<Runnable> blockingQueue = actualExecutor.getQueue();
        //得到被线程池拒绝过的任务数量
        long rejectCount = actualExecutor instanceof DynamicThreadPoolExecutor ? ((DynamicThreadPoolExecutor) actualExecutor).getRejectCountNum() : -1L;
        //创建ThreadPoolRunStateInfo对象，封装线程池运行信息
        ThreadPoolRunStateInfo stateInfo = ThreadPoolRunStateInfo.builder()
                 //设置线程池Id
                .tpId(threadPoolId)
                //设置线程活跃数量
                .activeSize(activeCount)
                //得到当前线程池中的线程数量
                .poolSize(actualExecutor.getPoolSize())
                //设置线程池已经执行完毕的任务数量
                .completedTaskCount(actualExecutor.getCompletedTaskCount())
                //设置线程池曾创建的最大线程数量
                .largestPoolSize(largestPoolSize)
                //计算线程池负载
                .currentLoad(CalculateUtil.divide(activeCount, actualExecutor.getMaximumPoolSize()) + "")
                //设置客户端的最新刷新时间
                .clientLastRefreshTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                //计算线程池的线程峰值
                .peakLoad(CalculateUtil.divide(largestPoolSize, actualExecutor.getMaximumPoolSize()) + "")
                //设置任务队列中任务数量
                .queueSize(blockingQueue.size())
                //设置任务队列剩余容量
                .queueRemainingCapacity(blockingQueue.remainingCapacity())
                //设置被拒绝过的任务数量
                .rejectCount(rejectCount)
                //设置当前时间戳
                .timestamp(System.currentTimeMillis())
                .build();
        //设置线程池的核心线程数
        stateInfo.setCoreSize(actualExecutor.getCorePoolSize());
        //设置线程池的最大线程数
        stateInfo.setMaximumSize(actualExecutor.getMaximumPoolSize());
        //设置任务队列名称
        stateInfo.setQueueType(blockingQueue.getClass().getSimpleName());
        //设置任务队列总大小
        stateInfo.setQueueCapacity(blockingQueue.size() + blockingQueue.remainingCapacity());
        //在这里掉用supplement方法，得到额外的线程池信息
        return supplement(stateInfo);
    }
}
