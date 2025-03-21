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

import top.panson.common.api.ClientCloseHookExecute;
import top.panson.common.config.ApplicationContextHolder;
import top.panson.common.constant.Constants;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/6
 * @方法描述：客户端关闭的时候，会向服务端发送请求，就是在DiscoveryClient对象的destroy方法中发送的关闭客户端请求
 * 请求会被当前控制器的clientCloseHook方法接收并处理
 */
@RestController
@RequestMapping(Constants.BASE_PATH + "/client/close")
public class ClientCloseHookController {

    @PostMapping
    public Result clientCloseHook(@RequestBody ClientCloseHookExecute.ClientCloseHookReq req) {
        Map<String, ClientCloseHookExecute> clientCloseHookExecuteMap = ApplicationContextHolder.getBeansOfType(ClientCloseHookExecute.class);
        //在这里清除服务端缓存的所有客户端线程池信息，这里我解释的比较笼统，其实分三个方面
        //清除配置信息的缓存，清除服务实例的缓存，清除第三方线程池缓存信息，这三个操作分别由ClientCloseHookExecute接口的三个实现类各自执行
        //在第二版本中我只为大家引入了ClientCloseHookRemoveNode实现类，在这个实现类中，会清除服务端缓存的对应服务实例信息
        clientCloseHookExecuteMap.forEach((key, execute) -> execute.closeHook(req));
        return Results.success();
    }
}
