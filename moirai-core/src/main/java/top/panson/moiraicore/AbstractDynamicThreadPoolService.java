package top.panson.moiraicore;



import top.panson.moiraicore.builder.ThreadPoolBuilder;
import top.panson.moiraicore.model.DynamicThreadPoolRegisterParameter;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



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