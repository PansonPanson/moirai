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

package top.panson.core.plugin.impl;

import top.panson.core.executor.ExtensibleThreadPoolExecutor;
import top.panson.core.plugin.PluginRuntime;
import top.panson.core.plugin.TaskAwarePlugin;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.core.task.TaskDecorator;

import java.util.ArrayList;
import java.util.List;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/28
 * @方法描述：装饰器插件对象，只有这个插件对象需要用户自己向里面添加真正的装饰器对象，任务装饰器对象也是用户自己定义的
 */
public class TaskDecoratorPlugin implements TaskAwarePlugin {

    public static final String PLUGIN_NAME = "task-decorator-plugin";

    /**
     * Get id.
     *
     * @return id
     */
    @Override
    public String getId() {
        return PLUGIN_NAME;
    }

    /**
     * Decorators
     */
    @Getter
    private final List<TaskDecorator> decorators = new ArrayList<>();

    /**
     * Callback when task is executed.
     *
     * @param runnable runnable
     * @return tasks to be execute
     * @see ExtensibleThreadPoolExecutor#execute
     */
    @Override
    public Runnable beforeTaskExecute(Runnable runnable) {
        for (TaskDecorator decorator : decorators) {
            runnable = decorator.decorate(runnable);
        }
        return runnable;
    }

    /**
     * Get plugin runtime info.
     *
     * @return plugin runtime info
     */
    @Override
    public PluginRuntime getPluginRuntime() {
        return new PluginRuntime(getId())
                .addInfo("decorators", decorators);
    }

    /**
     * Add a decorator.
     *
     * @param decorator decorator
     */
    public void addDecorator(@NonNull TaskDecorator decorator) {
        decorators.remove(decorator);
        decorators.add(decorator);
    }

    /**
     * Clear all decorators.
     */
    public void clearDecorators() {
        decorators.clear();
    }

    /**
     * Remove decorators.
     */
    public void removeDecorator(TaskDecorator decorator) {
        decorators.remove(decorator);
    }
}
