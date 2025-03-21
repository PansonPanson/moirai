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

import top.panson.common.config.ApplicationContextHolder;
import top.panson.common.web.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;


/**
 * @方法描述：web容器线程池选择器，这个选择器选择具体的web容器线程池的信息收集器来收集对应的web容器线程池信息
 * 在我为大家提供的第七版本代码中，这个选择器会选取tomcat的线程池处理器来收集tomcat线程池的信息
 */
@Slf4j
public class WebThreadPoolHandlerChoose {

    /**
     * Choose the web thread pool service bean.
     *
     * @return web thread pool service bean
     */
    public WebThreadPoolService choose() {
        WebThreadPoolService webThreadPoolService;
        try {
            webThreadPoolService = ApplicationContextHolder.getBean(WebThreadPoolService.class);
        } catch (Exception ex) {
            throw new ServiceException("Web thread pool service bean not found.", ex);
        }
        return webThreadPoolService;
    }
}
