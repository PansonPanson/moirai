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

package top.panson.console.controller;

import top.panson.common.toolkit.StringUtil;
import top.panson.common.toolkit.http.HttpUtil;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.config.model.biz.adapter.ThreadPoolAdapterReqDTO;
import top.panson.config.model.biz.adapter.ThreadPoolAdapterRespDTO;
import top.panson.config.service.ThreadPoolAdapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static top.panson.common.constant.Constants.REGISTER_ADAPTER_BASE_PATH;


/**
 * @方法描述：处理web页面框架线程池页面的所有请求
 */
@RequiredArgsConstructor
@RestController("threadPoolAdapterConsoleController")
public class ThreadPoolAdapterController {

    //缓存dubbo等线程池信息的对象
    private final ThreadPoolAdapterService threadPoolAdapterService;

    @GetMapping(REGISTER_ADAPTER_BASE_PATH + "/query")
    public Result<List<ThreadPoolAdapterRespDTO>> queryAdapterThreadPool(ThreadPoolAdapterReqDTO requestParameter) {
        List<ThreadPoolAdapterRespDTO> result = threadPoolAdapterService.query(requestParameter);
        return Results.success(result);
    }

    @GetMapping(REGISTER_ADAPTER_BASE_PATH + "/query/key")
    public Result<Set<String>> queryAdapterThreadPoolThreadPoolKey(ThreadPoolAdapterReqDTO requestParameter) {
        Set<String> result = threadPoolAdapterService.queryThreadPoolKey(requestParameter);
        return Results.success(result);
    }

    @PostMapping(REGISTER_ADAPTER_BASE_PATH + "/update")
    public Result<Void> updateAdapterThreadPool(@RequestBody ThreadPoolAdapterReqDTO requestParameter) {
        for (String each : requestParameter.getClientAddressList()) {
            String urlString = StringUtil.newBuilder("http://", each, "/adapter/thread-pool/update");
            HttpUtil.post(urlString, requestParameter);
        }
        return Results.success();
    }

}
