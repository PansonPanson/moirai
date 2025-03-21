package top.panson.common.executor.support;

import java.util.concurrent.BlockingQueue;

/**
 * Custom blocking-queue.
 */
public interface CustomBlockingQueue {

    /**
     * Gets the custom blocking queue type.
     *
     * @return
     */
    Integer getType();

    /**
     * Adapt hippo4j core blocking queue.
     *
     * @return
     */
    default String getName() {
        return "";
    }

    /**
     * Get custom blocking queue.
     *
     * @return
     */
    BlockingQueue generateBlockingQueue();
}