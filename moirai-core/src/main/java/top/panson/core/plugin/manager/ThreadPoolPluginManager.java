/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.panson.core.plugin.manager;

import top.panson.core.plugin.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/28
 * @方法描述：线程池的插件管理器接口
 */
public interface ThreadPoolPluginManager {

    /**
     * Get an empty manager.
     *
     * @return {@link EmptyThreadPoolPluginManager}
     */
    static ThreadPoolPluginManager empty() {
        return EmptyThreadPoolPluginManager.INSTANCE;
    }

    /**
     * Clear all.
     */
    void clear();

    /**
     * Get all registered plugins.
     *
     * @return plugins
     */
    Collection<ThreadPoolPlugin> getAllPlugins();

    /**
     * Register a {@link ThreadPoolPlugin}.
     *
     * @param plugin plugin
     * @throws IllegalArgumentException thrown when a plugin with the same {@link ThreadPoolPlugin#getId()}
     *                                  already exists in the registry
     * @see ThreadPoolPlugin#getId()
     */
    void register(ThreadPoolPlugin plugin);

    /**
     * Register plugin if it's not registered.
     *
     * @param plugin plugin
     * @return return true if successful register new plugin, false otherwise
     */
    boolean tryRegister(ThreadPoolPlugin plugin);

    /**
     * Whether the {@link ThreadPoolPlugin} has been registered.
     *
     * @param pluginId plugin id
     * @return ture if target has been registered, false otherwise
     */
    boolean isRegistered(String pluginId);

    /**
     * Unregister {@link ThreadPoolPlugin}.
     *
     * @param pluginId plugin id
     */
    void unregister(String pluginId);

    /**
     * Get {@link ThreadPoolPlugin}.
     *
     * @param pluginId plugin id
     * @param <A>      target aware type
     * @return {@link ThreadPoolPlugin}
     * @throws ClassCastException thrown when the object obtained by name cannot be converted to target type
     */
    <A extends ThreadPoolPlugin> Optional<A> getPlugin(String pluginId);

    /**
     * Get execute aware plugin list.
     *
     * @return {@link ExecuteAwarePlugin}
     */
    Collection<ExecuteAwarePlugin> getExecuteAwarePluginList();

    /**
     * Get rejected aware plugin list.
     *
     * @return {@link RejectedAwarePlugin}
     */
    Collection<RejectedAwarePlugin> getRejectedAwarePluginList();

    /**
     * Get shutdown aware plugin list.
     *
     * @return {@link ShutdownAwarePlugin}
     */
    Collection<ShutdownAwarePlugin> getShutdownAwarePluginList();

    /**
     * Get shutdown aware plugin list.
     *
     * @return {@link ShutdownAwarePlugin}
     */
    Collection<TaskAwarePlugin> getTaskAwarePluginList();

    // ==================== default methods ====================

    /**
     * Get plugin of type.
     *
     * @param pluginId   plugin id
     * @param pluginType plugin type
     * @return target plugin
     */
    default <A extends ThreadPoolPlugin> Optional<A> getPluginOfType(String pluginId, Class<A> pluginType) {
        return getPlugin(pluginId)
                .filter(pluginType::isInstance)
                .map(pluginType::cast);
    }

    /**
     * Get all plugins of type.
     *
     * @param pluginType plugin type
     * @return all plugins of type
     */
    default <A extends ThreadPoolPlugin> Collection<A> getAllPluginsOfType(Class<A> pluginType) {
        return getAllPlugins().stream()
                .filter(pluginType::isInstance)
                .map(pluginType::cast)
                .collect(Collectors.toList());
    }

    /**
     * Get {@link PluginRuntime} of all registered plugins.
     *
     * @return {@link PluginRuntime} of all registered plugins
     */
    default Collection<PluginRuntime> getAllPluginRuntimes() {
        return getAllPlugins().stream()
                .map(ThreadPoolPlugin::getPluginRuntime)
                .collect(Collectors.toList());
    }

    /**
     * Get {@link PluginRuntime} of registered plugin.
     *
     * @return {@link PluginRuntime} of registered plugin
     */
    default Optional<PluginRuntime> getRuntime(String pluginId) {
        return getPlugin(pluginId)
                .map(ThreadPoolPlugin::getPluginRuntime);
    }
}
