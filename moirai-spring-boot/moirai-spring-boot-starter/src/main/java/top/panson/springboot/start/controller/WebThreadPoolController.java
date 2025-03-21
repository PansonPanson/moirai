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

import top.panson.adapter.web.WebThreadPoolHandlerChoose;
import top.panson.adapter.web.WebThreadPoolService;
import top.panson.common.model.ThreadPoolBaseInfo;
import top.panson.common.model.ThreadPoolParameterInfo;
import top.panson.common.model.ThreadPoolRunStateInfo;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/9
 * @方法描述：这个控制器是专门用来和服务端的ThreadPoolController类发出的请求打交道的
 * 当用户需要在web界面得到web容器线程池的信息，服务端就会访问客户端，客户端使用当前端控制器对象收集web容器线程池信息
 * 然后返回给服务器，服务器再返回前端展示给用户
 */
@CrossOrigin
@RestController
@AllArgsConstructor
public class WebThreadPoolController {


    private final WebThreadPoolHandlerChoose webThreadPoolServiceChoose;

    //得到web容器线程池的运行信息
    @GetMapping("/web/base/info")
    public Result<ThreadPoolBaseInfo> getPoolBaseState(@RequestParam(value = "mark") String mark) {
        WebThreadPoolService webThreadPoolService = webThreadPoolServiceChoose.choose();
        if (webThreadPoolService != null && webThreadPoolService.getClass().getSimpleName().contains(mark)) {
            return Results.success(webThreadPoolService.simpleInfo());
        }
        return Results.success(null);
    }

    //得到web容器线程池的运行状态
    @GetMapping("/web/run/state")
    public Result<ThreadPoolRunStateInfo> getPoolRunState() {
        ThreadPoolRunStateInfo result = webThreadPoolServiceChoose.choose().getWebRunStateInfo();
        return Results.success(result);
    }

    //动态更新web容器线程池的方法
    @PostMapping("/web/update/pool")
    public Result<Void> updateWebThreadPool(@RequestBody ThreadPoolParameterInfo threadPoolParameterInfo) {
        webThreadPoolServiceChoose.choose().updateWebThreadPool(threadPoolParameterInfo);
        return Results.success();
    }
}
