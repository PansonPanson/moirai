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

package top.panson.config.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author:B站UP主陈清风扬，从零带你写框架系列教程的作者，个人微信号：chenqingfengyangjj。
 * @Description:系列教程目前包括手写Netty，XXL-JOB，Spring，RocketMq，Javac，JVM等课程。
 * @Date:2024/4/29
 * @Description:清理线程池历史信息的配置信息类
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = ServerBootstrapProperties.PREFIX)
public class ServerBootstrapProperties {

    public final static String PREFIX = "hippo4j.core";

    //是否定期清除历史信息
    private Boolean cleanHistoryDataEnable = Boolean.TRUE;

    //每次清除30分钟之内的
    private Integer cleanHistoryDataPeriod = 30;

    //netty服务器的端口号
    private String nettyServerPort = "8899";
}
