package top.panson.common.api;

import java.util.Map;

/**
 * Thread-pool dynamic refresh.
 */
public interface ThreadPoolDynamicRefresh {

    /**
     * Dynamic refresh.
     *
     * @param content
     */
    void dynamicRefresh(String content);

    /**
     * Dynamic refresh.
     *
     * @param content
     * @param newValueChangeMap
     */
    default void dynamicRefresh(String content, Map<String, Object> newValueChangeMap) {
    }
}