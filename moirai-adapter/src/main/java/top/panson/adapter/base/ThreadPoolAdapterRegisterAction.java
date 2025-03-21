package top.panson.adapter.base;

import java.util.List;
import java.util.Map;

/**
 * Provide registration for each adaptation
 */
public interface ThreadPoolAdapterRegisterAction {

    /**
     * Get thread pool adapter cache configs.
     *
     * @param threadPoolAdapterMap thread-pool adapter map
     * @return List<ThreadPoolAdapterCacheConfig>
     */
    List<ThreadPoolAdapterCacheConfig> getThreadPoolAdapterCacheConfigs(Map<String, ThreadPoolAdapter> threadPoolAdapterMap);

    /**
     * Do register.
     *
     * @param cacheConfigList cache config list
     */
    void doRegister(List<ThreadPoolAdapterCacheConfig> cacheConfigList);
}