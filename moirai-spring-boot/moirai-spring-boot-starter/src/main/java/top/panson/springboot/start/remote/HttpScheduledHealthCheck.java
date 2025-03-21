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

package top.panson.springboot.start.remote;

import top.panson.common.web.base.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static top.panson.common.constant.Constants.HEALTH_CHECK_PATH;
import static top.panson.common.constant.Constants.UP;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/6
 * @方法描述：健康检测器
 */
@Slf4j
@AllArgsConstructor
public class HttpScheduledHealthCheck extends AbstractHealthCheck {

    private final HttpAgent httpAgent;

    //在这个方法中客户端向服务端发送了健康检查请求
    @Override
    protected boolean sendHealthCheck() {
        boolean healthStatus = false;
        try {
            Result healthResult = httpAgent.httpGetSimple(HEALTH_CHECK_PATH);
            //健康检查成功的话，服务器会给客户端返回UP
            if (healthResult != null && Objects.equals(healthResult.getData(), UP)) {
                healthStatus = true;
            }
        } catch (Throwable ex) {
            log.error("Failed to periodically check the health status of the server. message: {}", ex.getMessage());
        }
        return healthStatus;
    }
}
