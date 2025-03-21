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

package top.panson.discovery.core;

import top.panson.common.api.ClientCloseHookExecute;
import top.panson.common.model.InstanceInfo;
import top.panson.common.toolkit.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/6
 * @方法描述：这个类中定义了客户端关闭时要执行的钩子方法，这个钩子方法执行的是清除InstanceRegistry中服务实例缓存信息的操作
 */
@Slf4j
@Component
@AllArgsConstructor
public class ClientCloseHookRemoveNode implements ClientCloseHookExecute {

    private final InstanceRegistry instanceRegistry;

    @Override
    public void closeHook(ClientCloseHookReq requestParam) {
        log.info("Remove Node, Execute client hook function. Request: {}", JSONUtil.toJSONString(requestParam));
        try {
            InstanceInfo instanceInfo = new InstanceInfo();
            //设置要删除的服务实例的信息
            instanceInfo.setAppName(requestParam.getAppName()).setInstanceId(requestParam.getInstanceId());
            //删除服务实例信息对象
            instanceRegistry.remove(instanceInfo);
        } catch (Exception ex) {
            log.error("Failed to delete node hook.", ex);
        }
    }
}
