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

package top.panson.springboot.start.monitor.collect;


import top.panson.common.model.ThreadPoolRunStateInfo;
import top.panson.common.monitor.AbstractMessage;
import top.panson.common.monitor.Message;
import top.panson.common.monitor.MessageTypeEnum;
import top.panson.common.monitor.RuntimeMessage;
import top.panson.common.toolkit.BeanUtil;
import top.panson.core.executor.manage.GlobalThreadPoolManage;

import top.panson.core.executor.state.AbstractThreadPoolRuntime;
import top.panson.springboot.start.config.BootstrapProperties;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static top.panson.core.toolkit.IdentifyUtil.getThreadPoolIdentify;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：线程池运行信息收集器
 */
@AllArgsConstructor
public class RunTimeInfoCollector extends AbstractThreadPoolRuntime implements Collector {

    private final BootstrapProperties properties;

    @Override
    public Message collectMessage() {
        //封装线程池运行信息的对象
        AbstractMessage message = new RuntimeMessage();
        List<Message> runtimeMessages = new ArrayList<>();
        //得到所有的动态线程池的Id
        List<String> listThreadPoolId = GlobalThreadPoolManage.listThreadPoolId();
        //遍历线程池Id
        for (String each : listThreadPoolId) {
            //得到每个线程池的运行信息
            ThreadPoolRunStateInfo poolRunState = getPoolRunState(each);
            //把运行信息转换为RuntimeMessage对象
            RuntimeMessage runtimeMessage = BeanUtil.convert(poolRunState, RuntimeMessage.class);
            //设置运行时信息对应的线程池标志，这个标志其实就是线程池Id+项目Id+租户Id
            runtimeMessage.setGroupKey(getThreadPoolIdentify(each, properties.getItemId(), properties.getNamespace()));
            //把线程池信息添加到runtimeMessages集合中
            runtimeMessages.add(runtimeMessage);
        }//设置消息类型为运行时
        message.setMessageType(MessageTypeEnum.RUNTIME);
        //设置全部的线程池运行时信息
        //这个信息会被发送到服务端
        message.setMessages(runtimeMessages);
        return message;
    }


    @Override
    public ThreadPoolRunStateInfo supplement(ThreadPoolRunStateInfo threadPoolRunStateInfo) {
        return threadPoolRunStateInfo;
    }
}
