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

package top.panson.springboot.start.controller;

import top.panson.common.api.ThreadDetailState;
import top.panson.common.model.ThreadDetailStateInfo;
import top.panson.common.model.ThreadPoolRunStateInfo;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.core.executor.state.ThreadPoolRunStateHandler;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：客户端直接收集动态线程池实时信息的控制器，这个控制器是和服务端对接的，当用户在web界面想要查看线程池的实时运行状况和调用栈
 * 服务端肯定是没有这些实时信息的，于是服务端就会发送向客户端发送请求，让客户端收集线程池的实时信息，然后返回给服务端，服务端再返回给前端用户
 */
@CrossOrigin
@RestController
@AllArgsConstructor
public class WebThreadPoolRunStateController {

    private final ThreadPoolRunStateHandler threadPoolRunStateHandler;

    private final ThreadDetailState threadDetailState;

    //得到动态线程池实时信息的方法
    @GetMapping("/run/state/{threadPoolId}")
    public Result<ThreadPoolRunStateInfo> getPoolRunState(@PathVariable("threadPoolId") String threadPoolId) {
        ThreadPoolRunStateInfo result = threadPoolRunStateHandler.getPoolRunState(threadPoolId);
        return Results.success(result);
    }

    //得到动态线程池执行任务的栈
    @GetMapping("/run/thread/state/{threadPoolId}")
    public Result<List<ThreadDetailStateInfo>> getThreadStateDetail(@PathVariable("threadPoolId") String threadPoolId) {
        List<ThreadDetailStateInfo> result = threadDetailState.getThreadDetailStateInfo(threadPoolId);
        return Results.success(result);
    }

}
