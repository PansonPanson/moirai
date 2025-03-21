package top.panson.springboot.start.core;

import top.panson.common.api.ThreadPoolDynamicRefresh;
import top.panson.common.executor.support.BlockingQueueTypeEnum;
import top.panson.core.executor.support.ThreadPoolBuilder;
import top.panson.springboot.start.config.BootstrapProperties;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @方法描述：客户端动态线程池订阅服务端配置信息的类
 */
@RequiredArgsConstructor
public class DynamicThreadPoolSubscribeConfig {

    //动态线程池配置信息刷新器
    private final ThreadPoolDynamicRefresh threadPoolDynamicRefresh;

    //长轮询对象
    private final ClientWorker clientWorker;

    //配置信息对象
    private final BootstrapProperties properties;

    //创建一个执行器，这个执行器是专门用来执行监听器中的任务的
    //既然要订阅服务端配置信息是否发生变化，肯定就要创建一个对应的监听器，使用观察者模式呀
    //这样一来，等客户端监听到服务端配置信息发生变化了，就会立刻调用监听器中的方法，而监听器中的方法就是动态刷新本地动态线程池的方法
    //这个刷新的操作就由下面创建的执行器来异步执行
    private final ExecutorService configRefreshExecutorService = ThreadPoolBuilder.builder()
            .corePoolSize(1)
            .maxPoolNum(2)
            .keepAliveTime(2000)
            .timeUnit(TimeUnit.MILLISECONDS)
            .workQueue(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
            .allowCoreThreadTimeOut(true)
            .threadFactory("client.dynamic.threadPool.change.config")
            .rejected(new ThreadPoolExecutor.AbortPolicy())
            .build();


    //订阅服务端线程池配置信息的方法
    public void subscribeConfig(String threadPoolId) {
        //在这里把要坚挺的动态线程池的id和对应的监听器方法都传进去了
        subscribeConfig(threadPoolId, threadPoolDynamicRefresh::dynamicRefresh);
    }


    //订阅服务端线程池配置信息的方法，这个方法比上面方法多了一个功能，那就是用户可以自己定义监听器方法
    public void subscribeConfig(String threadPoolId, ThreadPoolSubscribeCallback threadPoolSubscribeCallback) {
        //创建监听器对象
        Listener configListener = new Listener() {

            //监听器要执行的回调方法
            @Override
            public void receiveConfigInfo(String config) {
                threadPoolSubscribeCallback.callback(config);
            }

            //执行监听器方法时用到的执行器
            @Override
            public Executor getExecutor() {
                return configRefreshExecutorService;
            }
        };
        //在这里把监听器添加到长轮询对象中了，实际上会在长轮询对象中为监听器创建一个对应的CacheData对象，监听器对象就交给CacheData对象持有
        //当监听器对象中的方法要被执行时，就会调用CacheData对象的safeNotifyListener方法，在safeNotifyListener方法中，先获得监听器的执行器
        //然后使用执行器执行监听器的receiveConfigInfo方法刷新本地线程池配置信息
        clientWorker.addTenantListeners(properties.getNamespace(), properties.getItemId(), threadPoolId, Arrays.asList(configListener));
    }
}
