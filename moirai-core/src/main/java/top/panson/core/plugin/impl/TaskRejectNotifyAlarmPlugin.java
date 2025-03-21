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
import top.panson.core.executor.ExtensibleThreadPoolExecutor;
import top.panson.core.plugin.RejectedAwarePlugin;

import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;



/**
 *
 * @方法描述：拒绝任务的时候执行通知告警操作的插件
 */
public class TaskRejectNotifyAlarmPlugin implements RejectedAwarePlugin {

    public static final String PLUGIN_NAME = "task-reject-notify-alarm-plugin";

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
     * @方法描述：任务被拒绝时直接发送用户告警消息
     */
    @Override
    public void beforeRejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        if (!(executor instanceof ExtensibleThreadPoolExecutor)) {
            return;
        }
        String threadPoolId = ((ExtensibleThreadPoolExecutor) executor).getThreadPoolId();
        Optional.ofNullable(ApplicationContextHolder.getInstance())
                .map(context -> context.getBean(ThreadPoolCheckAlarm.class))
                .ifPresent(handler -> handler.asyncSendRejectedAlarm(threadPoolId));
    }
}
