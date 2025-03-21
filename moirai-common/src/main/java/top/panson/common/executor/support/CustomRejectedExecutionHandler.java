package top.panson.common.executor.support;

import java.util.concurrent.RejectedExecutionHandler;

/**
 * Custom rejected execution handler.
 */
public interface CustomRejectedExecutionHandler {

    /**
     * Get custom reject policy type.
     *
     * @return
     */
    Integer getType();

    /**
     * Adapt hippo-4j core rejected execution handler.
     *
     * @return
     */
    default String getName() {
        return "";
    }

    /**
     * Get custom reject policy.
     *
     * @return
     */
    RejectedExecutionHandler generateRejected();
}