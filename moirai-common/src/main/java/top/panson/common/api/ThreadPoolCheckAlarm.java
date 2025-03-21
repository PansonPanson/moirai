package top.panson.common.api;

import org.springframework.boot.CommandLineRunner;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Thread-pol check alarm.
 *
 * <p>Dynamic thread pool check and send logic wait for refactoring,
 * Try not to rely on this component for custom extensions, because it is undefined.
 */
public interface ThreadPoolCheckAlarm extends CommandLineRunner {

    /**
     * Check pool capacity alarm.
     *
     * @param threadPoolId       thread-pool id
     * @param threadPoolExecutor thread-pool executor
     */
    void checkPoolCapacityAlarm(String threadPoolId, ThreadPoolExecutor threadPoolExecutor);

    /**
     * Check pool activity alarm.
     *
     * @param threadPoolId       thread-pool id
     * @param threadPoolExecutor thread-pool executor
     */
    void checkPoolActivityAlarm(String threadPoolId, ThreadPoolExecutor threadPoolExecutor);

    /**
     * Async send rejected alarm.
     *
     * @param threadPoolId thread-pool id
     */
    void asyncSendRejectedAlarm(String threadPoolId);

    /**
     * Async send execute time-out alarm.
     *
     * @param threadPoolId       thread-pool id
     * @param executeTime        execute time
     * @param executeTimeOut     execute time-out
     * @param threadPoolExecutor thread-pool executor
     */
    void asyncSendExecuteTimeOutAlarm(String threadPoolId, long executeTime, long executeTimeOut, ThreadPoolExecutor threadPoolExecutor);
}
