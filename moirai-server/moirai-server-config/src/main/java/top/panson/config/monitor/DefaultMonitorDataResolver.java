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

import top.panson.common.monitor.Message;
import top.panson.common.monitor.MessageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 *
 * @方法描述：默认的监控数据解析器，在第六版本代码中并不会使用这个解析器来解析客户端上报的线程池运行信息
 * 使用的是RuntimeDataResolver这个解析器
 */
@Slf4j
@Component
public class DefaultMonitorDataResolver extends AbstractMonitorDataExecuteStrategy<Message> {

    @Override
    public String mark() {
        return MessageTypeEnum.DEFAULT.name();
    }

    @Override
    public void execute(Message message) {
        log.warn("There is no suitable monitoring data storage actuator.");
    }
}
