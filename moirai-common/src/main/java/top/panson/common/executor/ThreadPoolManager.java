package top.panson.common.executor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Thread-pool manager.
 */
public final class ThreadPoolManager {

    private Map<String, Map<String, Set<ExecutorService>>> resourcesManager;

    private Map<String, Object> lockers = new ConcurrentHashMap();

    private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();

    public static ThreadPoolManager getInstance() {
        return INSTANCE;
    }

    private ThreadPoolManager() {
    }

    static {
        INSTANCE.init();
    }

    private void init() {
        resourcesManager = new ConcurrentHashMap();
    }

    public void register(String tenantId, String group, ExecutorService executor) {
        if (!resourcesManager.containsKey(tenantId)) {
            synchronized (this) {
                lockers.put(tenantId, new Object());
            }
        }
        final Object monitor = lockers.get(tenantId);
        synchronized (monitor) {
            Map<String, Set<ExecutorService>> map = resourcesManager.get(tenantId);
            if (map == null) {
                map = new HashMap();
                map.put(group, new HashSet());
                map.get(group).add(executor);
                resourcesManager.put(tenantId, map);
                return;
            }
            if (!map.containsKey(group)) {
                map.put(group, new HashSet());
            }
            map.get(group).add(executor);
        }
    }
}
