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

package top.panson.config.monitor;

import top.panson.common.monitor.MessageTypeEnum;
import top.panson.common.monitor.RuntimeMessage;
import top.panson.config.service.biz.HisRunDataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;




/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：线程池运行时信息解析器
 */
@Slf4j
@Component
@AllArgsConstructor
public class RuntimeDataResolver extends AbstractMonitorDataExecuteStrategy<RuntimeMessage> {

    private final HisRunDataService hisRunDataService;

    @Override
    public String mark() {
        return MessageTypeEnum.RUNTIME.name();
    }

    //这个解析器所做的就是把线程运行时信息直接存放到数据库中
    @Override
    public void execute(RuntimeMessage message) {
        hisRunDataService.save(message);
    }
}
