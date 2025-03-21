package top.panson.springboot.start.core;

import java.util.concurrent.Executor;

/**
 * Listener.
 */
public interface Listener {

    /**
     * Get executor.
     *
     * @return executor
     */
    Executor getExecutor();

    /**
     * Receive config info.
     *
     * @param configInfo config info
     */
    void receiveConfigInfo(String configInfo);
}