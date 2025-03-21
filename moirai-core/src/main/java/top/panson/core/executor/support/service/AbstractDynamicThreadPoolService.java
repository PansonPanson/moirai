package top.panson.core.executor.support.service;

import top.panson.common.executor.support.BlockingQueueTypeEnum;
import top.panson.common.executor.support.RejectedPolicyTypeEnum;
import top.panson.common.model.register.DynamicThreadPoolRegisterParameter;
import top.panson.core.executor.support.ThreadPoolBuilder;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



/**
 *
 * @方法描述：源码中这个类中有一个严重的bug，会导致程序无法运行，但现在没必要改了，而且也不应该在这里改，我修改后的程序用不到这个类的方法了，所以就这么着吧
 */
public abstract class AbstractDynamicThreadPoolService implements DynamicThreadPoolService {


    //源码中这个方法内有好几个空指针异常
    public ThreadPoolExecutor buildDynamicThreadPoolExecutor(DynamicThreadPoolRegisterParameter registerParameter) {
        ThreadPoolExecutor dynamicThreadPoolExecutor = ThreadPoolBuilder.builder()
                .threadPoolId(registerParameter.getThreadPoolId())
                .corePoolSize(registerParameter.getCorePoolSize())
                .maxPoolNum(registerParameter.getMaximumPoolSize())
                .workQueue(BlockingQueueTypeEnum.createBlockingQueue(registerParameter.getBlockingQueueType().getType(), registerParameter.getCapacity()))
                .threadFactory(registerParameter.getThreadNamePrefix())
                .keepAliveTime(registerParameter.getKeepAliveTime(), TimeUnit.SECONDS)
                .executeTimeOut(registerParameter.getExecuteTimeOut())
                .rejected(RejectedPolicyTypeEnum.createPolicy(registerParameter.getRejectedPolicyType().getType()))
                .dynamicPool()
                .build();
        return dynamicThreadPoolExecutor;
    }
}