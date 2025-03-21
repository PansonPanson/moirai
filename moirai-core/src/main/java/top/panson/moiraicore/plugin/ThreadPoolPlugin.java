package top.panson.moiraicore.plugin;


import top.panson.moiraicore.model.PluginRuntime;

public interface ThreadPoolPlugin {

    /**
     * Get id.
     *
     * @return id
     */
    String getId();

    /**
     * Callback when plugin register into manager
     *
     * @see ThreadPoolPluginManager#register
     */
    default void start() {
    }

    /**
     * Callback when plugin unregister from manager
     *
     * @see ThreadPoolPluginManager#unregister
     * @see ThreadPoolPluginManager#clear
     */
    default void stop() {
    }

    /**
     * Get plugin runtime info.
     *
     * @return plugin runtime info
     */
    default PluginRuntime getPluginRuntime() {
        return new PluginRuntime(getId());
    }
}
