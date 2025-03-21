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

package top.panson.core.executor.support.adpter;

import top.panson.common.toolkit.ReflectUtil;
import top.panson.core.executor.DynamicThreadPoolExecutor;

import java.util.Objects;
import java.util.concurrent.Executor;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/27
 * @方法描述：ExecutorTtlWrapper对象的适配器，可以从ExecutorTtlWrapper对象中获得其持有的DynamicThreadPoolExecutor对象
 */
public class TransmittableThreadLocalExecutorAdapter implements DynamicThreadPoolAdapter {

    private static String MATCH_CLASS_NAME = "ExecutorTtlWrapper";

    private static String FIELD_NAME = "executor";


    //判断传进来的对象是否和当前适配器器对象匹配
    @Override
    public boolean match(Object executor) {
        return Objects.equals(MATCH_CLASS_NAME, executor.getClass().getSimpleName());
    }


    //从ExecutorTtlWrapper对象中获得其持有的DynamicThreadPoolExecutor对象
    @Override
    public DynamicThreadPoolExecutor unwrap(Object executor) {
        Object unwrap = ReflectUtil.getFieldValue(executor, FIELD_NAME);
        if (unwrap != null && unwrap instanceof DynamicThreadPoolExecutor) {
            return (DynamicThreadPoolExecutor) unwrap;
        }
        return null;
    }


    //将dynamicThreadPoolExecutor对象替换到executor中
    @Override
    public void replace(Object executor, Executor dynamicThreadPoolExecutor) {
        ReflectUtil.setFieldValue(executor, FIELD_NAME, dynamicThreadPoolExecutor);
    }
}
