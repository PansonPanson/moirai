package top.panson.common.constant;

/**
 * Change thread-pool constants.
 */
public class ChangeThreadPoolConstants {

    /**
     * Dynamic thread pool parameter change text
     */
    public static final String CHANGE_THREAD_POOL_TEXT = "[{}] Dynamic thread pool change parameter."
            + "\n    corePoolSize: {}"
            + "\n    maximumPoolSize: {}"
            + "\n    capacity: {}"
            + "\n    keepAliveTime: {}"
            + "\n    executeTimeOut: {}"
            + "\n    rejectedType: {}"
            + "\n    allowCoreThreadTimeOut: {}";

    /**
     * Dynamic thread pool parameter change separator
     */
    public static final String CHANGE_DELIMITER = "%s => %s";
}