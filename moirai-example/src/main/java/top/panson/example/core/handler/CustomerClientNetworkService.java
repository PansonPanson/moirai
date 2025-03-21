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

package top.panson.example.core.handler;

import top.panson.common.api.ClientNetworkService;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Customer client network service.
 */
public class CustomerClientNetworkService implements ClientNetworkService {

    @Override
    public String[] getNetworkIpPort(ConfigurableEnvironment environment) {
        String[] network = new String[2];
        //这里为什么把客户端地址写成回环地址？而且是硬编码，这样一来只有客户端和服务端在同一台机器上才能测试成功了？
        network[0] = "127.0.0.1";
        network[1] = environment.getProperty("server.port", "1994");
        return network;
    }
}
