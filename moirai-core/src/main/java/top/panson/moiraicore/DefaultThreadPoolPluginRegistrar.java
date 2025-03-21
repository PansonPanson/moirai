package top.panson.moiraicore;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import top.panson.moiraicore.plugin.*;


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