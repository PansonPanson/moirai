package top.panson.common.executor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Executor factory.
 */
public class ExecutorFactory {

    /**
     * Thread pool management tool.
     */
    public static final class Managed {

        private static final String DEFAULT_NAMESPACE = "dynamic.thread-pool";

        private static final ThreadPoolManager THREAD_POOL_MANAGER = ThreadPoolManager.getInstance();

        public static ScheduledExecutorService newSingleScheduledExecutorService(String group, ThreadFactory threadFactory) {
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, threadFactory);
            THREAD_POOL_MANAGER.register(DEFAULT_NAMESPACE, group, executorService);
            return executorService;
        }
    }
}
