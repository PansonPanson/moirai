package top.panson.moiraicore;


/**
 * Dynamic thread-pool service.
 */
public interface DynamicThreadPoolService {

    /**
     * Registering dynamic thread pools at runtime.
     *
     * @param registerWrapper register wrapper
     * @return dynamic thread-pool executor
     */
    void registerDynamicThreadPool(DynamicThreadPoolRegisterWrapper registerWrapper);
}