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

package top.panson.discovery.controller;

import top.panson.common.model.InstanceInfo;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.common.web.exception.ErrorCodeEnum;
import top.panson.discovery.core.InstanceRegistry;
import top.panson.discovery.core.Lease;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static top.panson.common.constant.Constants.BASE_PATH;


/**
 * @方法描述：服务发现控制器，客户端向服务端发送服务发现的请求后，都会被当前的控制器接收并处理
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(BASE_PATH + "/apps")
public class ApplicationController {

    //服务实例注册表，所有服务实例都会注册到这个注册表对象中
    private final InstanceRegistry<InstanceInfo> instanceRegistry;


    //得到所有注册到服务端的服务实例
    @GetMapping("/{appName}")
    public Result<List<Lease<InstanceInfo>>> applications(@PathVariable String appName) {
        List<Lease<InstanceInfo>> resultInstanceList = instanceRegistry.listInstance(appName);
        return Results.success(resultInstanceList);
    }


    //添加一个服务实例信息到注册表中，客户端发送的注册请求就会被这个方法处理
    @PostMapping("/register")
    public Result<Void> addInstance(@RequestBody InstanceInfo instanceInfo) {
        instanceRegistry.register(instanceInfo);
        return Results.success();
    }

    //处理客户端服务实例的续约请求的方法
    @PostMapping("/renew")
    public Result<Void> renew(@RequestBody InstanceInfo.InstanceRenew instanceRenew) {
        //执行续约操作
        boolean isSuccess = instanceRegistry.renew(instanceRenew);
        if (!isSuccess) {
            log.warn("Not Found (Renew): {} - {}", instanceRenew.getAppName(), instanceRenew.getInstanceId());
            return Results.failure(ErrorCodeEnum.NOT_FOUND);
        }
        return Results.success();
    }


    //从注册表中移除对应的服务实例信息
    @PostMapping("/remove")
    public Result<Void> remove(@RequestBody InstanceInfo instanceInfo) {
        instanceRegistry.remove(instanceInfo);
        return Results.success();
    }
}
