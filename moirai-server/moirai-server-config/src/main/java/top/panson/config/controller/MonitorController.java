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

import top.panson.common.constant.Constants;
import top.panson.common.monitor.MessageWrapper;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.config.model.biz.monitor.MonitorActiveRespDTO;
import top.panson.config.model.biz.monitor.MonitorQueryReqDTO;
import top.panson.config.model.biz.monitor.MonitorRespDTO;
import top.panson.config.service.biz.HisRunDataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：这个控制器处理的是web界面线程池监控页面的请求
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(Constants.BASE_PATH + "/monitor")
public class MonitorController {


    private final HisRunDataService hisRunDataService;


    @GetMapping
    public Result<List<MonitorRespDTO>> queryMonitor(MonitorQueryReqDTO reqDTO) {
        List<MonitorRespDTO> monitorRespList = hisRunDataService.query(reqDTO);
        return Results.success(monitorRespList);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：当用户在web界面点击线程池监控页面，并且查询具体线程池的监控信息时，就会访问后端的这个接口，然后调用
     * hisRunDataService.queryInfoThreadPoolMonitor(reqDTO)方法把详细数据返回给前端
     */
    @PostMapping("/info")
    public Result<MonitorActiveRespDTO> queryInfoThreadPoolMonitor(@RequestBody MonitorQueryReqDTO reqDTO) {
        MonitorActiveRespDTO monitorRespList = hisRunDataService.queryInfoThreadPoolMonitor(reqDTO);
        return Results.success(monitorRespList);
    }


    @PostMapping("/last/task/count")
    public Result<MonitorRespDTO> queryThreadPoolLastTaskCount(@RequestBody MonitorQueryReqDTO reqDTO) {
        MonitorRespDTO resultDTO = hisRunDataService.queryThreadPoolLastTaskCount(reqDTO);
        return Results.success(resultDTO);
    }


    @PostMapping
    public Result<Void> dataCollect(@RequestBody MessageWrapper messageWrapper) {
        return hisRunDataService.dataCollect(messageWrapper);
    }
}
