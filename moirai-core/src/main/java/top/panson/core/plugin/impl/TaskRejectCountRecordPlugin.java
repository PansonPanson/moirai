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

import top.panson.core.plugin.PluginRuntime;
import top.panson.core.plugin.RejectedAwarePlugin;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/28
 * @方法描述：拒绝策略处理器的扩展插件
 */
public class TaskRejectCountRecordPlugin implements RejectedAwarePlugin {

    public static final String PLUGIN_NAME = "task-reject-count-record-plugin";

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
     * Rejection count
     */
    @Setter
    @Getter
    private AtomicLong rejectCount = new AtomicLong(0);

    /**
     * Get plugin runtime info.
     *
     * @return plugin runtime info
     */
    @Override
    public PluginRuntime getPluginRuntime() {
        return new PluginRuntime(getId())
                .addInfo("rejectCount", getRejectCountNum());
    }

    /**
     * Record rejection count.
     *
     * @param r        task
     * @param executor executor
     */
    @Override
    public void beforeRejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        rejectCount.incrementAndGet();
    }

    /**
     * Get reject count num.
     *
     * @return reject count num
     */
    public Long getRejectCountNum() {
        return rejectCount.get();
    }
}
