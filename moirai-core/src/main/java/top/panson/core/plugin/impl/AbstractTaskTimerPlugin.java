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

import top.panson.core.plugin.ExecuteAwarePlugin;
import top.panson.core.toolkit.SystemClock;

import java.util.Optional;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/28
 * @方法描述：计算任务执行是否超时的插件
 */
public abstract class AbstractTaskTimerPlugin implements ExecuteAwarePlugin {


    private final ThreadLocal<Long> startTimes = new ThreadLocal<>();


    //任务开始执行之前会执行这个方法，在这个方法中把任务开始执行时间放到线程本地map中
    @Override
    public final void beforeExecute(Thread thread, Runnable runnable) {
        startTimes.set(currentTime());
    }


    //该方法会在任务执行之后被调用
    @Override
    public final void afterExecute(Runnable runnable, Throwable throwable) {
        try {
            //从线程本地map中得到任务的开始时间
            Optional.ofNullable(startTimes.get())
                    //计算出耗时时间
                    .map(startTime -> currentTime() - startTime)
                    //交给processTaskTime方法处理，这里就会来到具体的子类的中
                    //也就是TaskTimeoutNotifyAlarmPlugin类中
                    .ifPresent(this::processTaskTime);
        } finally {
            //清除线程本地map
            startTimes.remove();
        }
    }

    /**
     * Get the current time.
     *
     * @return current time
     */
    protected long currentTime() {
        return SystemClock.now();
    }

    /**
     * Processing the execution time of the task.
     *
     * @param taskExecuteTime execute time of task
     */
    protected abstract void processTaskTime(long taskExecuteTime);
}
