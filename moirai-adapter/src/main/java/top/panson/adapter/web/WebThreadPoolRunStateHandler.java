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

package top.panson.adapter.web;

import top.panson.common.model.ThreadPoolRunStateInfo;
import top.panson.common.toolkit.ByteConvertUtil;
import top.panson.common.toolkit.MemoryUtil;
import top.panson.common.toolkit.StringUtil;
import top.panson.core.executor.state.AbstractThreadPoolRuntime;
import lombok.extern.slf4j.Slf4j;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/9
 * @方法描述：完善web容器线程池额外信息的类，设置内存使用率，剩余内存等等信息
 */
@Slf4j
public class WebThreadPoolRunStateHandler extends AbstractThreadPoolRuntime {

    @Override
    public ThreadPoolRunStateInfo supplement(ThreadPoolRunStateInfo poolRunStateInfo) {
        long used = MemoryUtil.heapMemoryUsed();
        long max = MemoryUtil.heapMemoryMax();
        String memoryProportion = StringUtil.newBuilder(
                "Allocation: ",
                ByteConvertUtil.getPrintSize(used),
                " / Maximum available: ",
                ByteConvertUtil.getPrintSize(max));
        poolRunStateInfo.setCurrentLoad(poolRunStateInfo.getCurrentLoad() + "%");
        poolRunStateInfo.setPeakLoad(poolRunStateInfo.getPeakLoad() + "%");
        poolRunStateInfo.setMemoryProportion(memoryProportion);
        poolRunStateInfo.setFreeMemory(ByteConvertUtil.getPrintSize(Math.subtractExact(max, used)));
        return poolRunStateInfo;
    }
}
