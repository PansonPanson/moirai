package top.panson.common.api;

import top.panson.common.model.ThreadDetailStateInfo;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Get thread status in thread pool.
 */
public interface ThreadDetailState {

    /**
     * Get thread status in thread pool.
     *
     * @param threadPoolId
     * @return
     */
    List<ThreadDetailStateInfo> getThreadDetailStateInfo(String threadPoolId);

    /**
     * Get thread status in thread pool.
     *
     * @param threadPoolExecutor
     * @return
     */
    List<ThreadDetailStateInfo> getThreadDetailStateInfo(ThreadPoolExecutor threadPoolExecutor);
}