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

import top.panson.common.api.ThreadPoolCheckAlarm;
import top.panson.common.config.ApplicationContextHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;



/**
 *
 * @方法描述：这个插件就是用来判断线程中的任务执行是否超时了，如果超时了就调用processTaskTime方法执行告警操作
 */
@AllArgsConstructor
public class TaskTimeoutNotifyAlarmPlugin extends AbstractTaskTimerPlugin {

    public static final String PLUGIN_NAME = "task-timeout-notify-alarm-plugin";

    /**
     * Thread-pool id
     */
    private final String threadPoolId;

    /**
     * Execute time-out
     */
    @Getter
    @Setter
    private Long executeTimeOut;

    /**
     * Thread-pool executor
     */
    private final ThreadPoolExecutor threadPoolExecutor;

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
     * @方法描述：线程池执行任务超时，要发送告警消息给用户。
     */
    @Override
    protected void processTaskTime(long taskExecuteTime) {
        if (taskExecuteTime <= executeTimeOut) {
            return;
        }
        Optional.ofNullable(ApplicationContextHolder.getInstance())
                .map(context -> context.getBean(ThreadPoolCheckAlarm.class))
                .ifPresent(handler -> handler.asyncSendExecuteTimeOutAlarm(
                        threadPoolId, taskExecuteTime, executeTimeOut, threadPoolExecutor));
    }
}
