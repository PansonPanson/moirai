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

package top.panson.core.enable;

import top.panson.common.toolkit.StringUtil;
import top.panson.core.config.BootstrapPropertiesInterface;
import top.panson.core.config.ConfigEmptyException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Objects;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/28
 * @方法描述：对项目配置做校验的对象，就是dynamicThreadPoolBeforeCheckBean这个对象提供了这个功能
 */
@Configuration
@AllArgsConstructor
public class BeforeCheckConfiguration {

    private final String bootstrapPropertiesClassName = "top.panson.springboot.starter.config.BootstrapProperties";

    @Bean
    public BeforeCheck dynamicThreadPoolBeforeCheckBean(@Autowired(required = false) BootstrapPropertiesInterface properties,
                                                                                 ConfigurableEnvironment environment) {
        boolean checkFlag = properties != null && Objects.equals(bootstrapPropertiesClassName, properties.getClass().getName()) && properties.getEnable();
        if (checkFlag) {//对命名空间做判断
            String namespace = properties.getNamespace();
            if (StringUtil.isBlank(namespace)) {
                throw new ConfigEmptyException(
                        "Web server failed to start. The dynamic thread pool namespace is empty.",
                        "Please check whether the [spring.dynamic.thread-pool.namespace] configuration is empty or an empty string.");
            }//对项目Id做判断
            String itemId = properties.getItemId();
            if (StringUtil.isBlank(itemId)) {
                throw new ConfigEmptyException(
                        "Web server failed to start. The dynamic thread pool item id is empty.",
                        "Please check whether the [spring.dynamic.thread-pool.item-id] configuration is empty or an empty string.");
            }//对服务器地址做判断
            String serverAddr = properties.getServerAddr();
            if (StringUtil.isBlank(serverAddr)) {
                throw new ConfigEmptyException(
                        "Web server failed to start. The dynamic thread pool server addr is empty.",
                        "Please check whether the [spring.dynamic.thread-pool.server-addr] configuration is empty or an empty string.");
            }//对项目名称做判断
            String applicationName = environment.getProperty("spring.application.name");
            if (StringUtil.isBlank(applicationName)) {
                throw new ConfigEmptyException(
                        "Web server failed to start. The dynamic thread pool application name is empty.",
                        "Please check whether the [spring.application.name] configuration is empty or an empty string.");
            }
        }
        return new BeforeCheck();
    }

    public class BeforeCheck {

    }
}
