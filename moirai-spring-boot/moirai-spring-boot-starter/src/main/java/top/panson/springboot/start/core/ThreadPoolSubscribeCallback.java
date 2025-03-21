package top.panson.springboot.start.core;

/**
 * ThreadPool subscribe callback.
 */
public interface ThreadPoolSubscribeCallback {

    /**
     * Callback.
     *
     * @param config config info
     */
    void callback(String config);
}
