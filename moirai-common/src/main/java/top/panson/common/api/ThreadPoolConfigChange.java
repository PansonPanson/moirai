package top.panson.common.api;

/**
 * Thread-pool config change.
 */
public interface ThreadPoolConfigChange<T extends NotifyRequest> {

    /**
     * Send pool config change.
     *
     * @param requestParam request param
     */
    void sendPoolConfigChange(T requestParam);
}