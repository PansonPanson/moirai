package top.panson.core.plugin.manager;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import top.panson.core.plugin.impl.*;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/28
 * @方法描述：默认的线程池插件注册器，通过这个注册器对象，把线程池的所有插件都注册到线程池的插件管理器中
 */
@NoArgsConstructor
@AllArgsConstructor
public class DefaultThreadPoolPluginRegistrar implements ThreadPoolPluginRegistrar {

    /**
     * Execute time out
     */
    private long executeTimeOut;

    /**
     * Await termination millis
     */
    private long awaitTerminationMillis;

    /**
     * Create and register plugin for the specified thread-pool instance.
     *
     * @param support thread pool plugin manager delegate
     */
    @Override
    public void doRegister(ThreadPoolPluginSupport support) {
        support.register(new TaskDecoratorPlugin());
        support.register(new TaskTimeoutNotifyAlarmPlugin(support.getThreadPoolId(), executeTimeOut, support.getThreadPoolExecutor()));
        support.register(new TaskRejectCountRecordPlugin());
        support.register(new TaskRejectNotifyAlarmPlugin());
        support.register(new ThreadPoolExecutorShutdownPlugin(awaitTerminationMillis));
    }
}