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

package top.panson.config.controller;

import top.panson.adapter.base.ThreadPoolAdapterCacheConfig;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.config.service.ThreadPoolAdapterService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static top.panson.common.constant.Constants.REGISTER_ADAPTER_PATH;


/**
 *
 * @方法描述：客户端收集到的dubbo等框架的线程池的信息会通过这个控制器中的方法注册到服务端
 * 最后保存在ThreadPoolAdapterService对象中
 */
@RestController
@AllArgsConstructor
public class ThreadPoolAdapterController {

    private final ThreadPoolAdapterService threadPoolAdapterService;

    @PostMapping(REGISTER_ADAPTER_PATH)
    public Result registerAdapterThreadPool(@RequestBody List<ThreadPoolAdapterCacheConfig> requestParameter) {
        threadPoolAdapterService.register(requestParameter);
        return Results.success();
    }
}
