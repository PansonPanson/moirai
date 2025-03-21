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
import top.panson.common.model.register.DynamicThreadPoolRegisterWrapper;

import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.config.model.ConfigAllInfo;
import top.panson.config.model.ConfigInfoBase;
import top.panson.config.service.ConfigServletInner;
import top.panson.config.service.biz.ConfigService;
import top.panson.config.toolkit.Md5ConfigUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Map;

/**
 *
 * @Description:处理线程池配置信息的控制器，这个控制器引入进来是因为当客户端创建动态线程池的时候
 * 会先访问一次服务端，看看服务端的数据库中是否存在线程池的配置信息，如果存在就使用数据库中的信息
 * 刷新客户端动态线程池
 */
@RestController
@AllArgsConstructor
@RequestMapping(Constants.CONFIG_CONTROLLER_PATH)
public class ConfigController {

    private final ConfigService configService;

    //处理长轮询请求的对象
    private final ConfigServletInner configServletInner;


    //这个方法就是用来处理查询线程池配置信息请求的
    @GetMapping
    public Result<ConfigInfoBase> detailConfigInfo(@RequestParam("tpId") String tpId,
                                                   @RequestParam("itemId") String itemId,
                                                   @RequestParam("namespace") String namespace,
                                                   @RequestParam(value = "instanceId", required = false) String instanceId) {
        ConfigAllInfo configAllInfo = configService.findConfigRecentInfo(tpId, itemId, namespace, instanceId);
        return Results.success(configAllInfo);
    }


    //配置变更的入口方法，这个方法是专门处理web界面线程池实例中，修改线程池配置信息请求的方法
    @PostMapping
    public Result<Boolean> publishConfig(@RequestParam(value = "identify", required = false) String identify,
                                         @RequestBody ConfigAllInfo config) {
        configService.insertOrUpdate(identify, true, config);
        return Results.success(true);
    }

    //长轮询的请求会被这个方法处理
    @SneakyThrows
    @PostMapping("/listener")
    public void listener(HttpServletRequest request, HttpServletResponse response) {
        //设置支持处理异步请求标志
        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        //获得要监听的配置信息，这里就把在客户端封装好的所有要监听的线程池信息的字符串得到了
        String probeModify = request.getParameter(Constants.LISTENING_CONFIGS);
        if (StringUtils.isEmpty(probeModify)) {
            throw new IllegalArgumentException("invalid probeModify");
        }
        //解码字符串
        probeModify = URLDecoder.decode(probeModify, Constants.ENCODE);
        //这个map会存放要监听的各个线程池信息
        Map<String, String> clientMd5Map;
        try {
            //解析probeModify，把probeModify中的信息封装到一个map中，然后把map赋值给clientMd5Map
            //这里最后得到的map中的key就是threadPoolId+itemId+tenantId+identify，value就是线程池的md5
            //这里我想多解释一句，其实判断客户端和服务端的配置是否相同，对比线程池的md5即可
            //因为线程池的md5就是根据线程池的配置参数得到的，只要配置参数发生了变化，md5肯定就会发生变化
            //所以只要服务端的md5和客户端线程池的md5不一样，就意味着服务端线程池的配置更新了
            clientMd5Map = Md5ConfigUtil.getClientMd5Map(probeModify);
        } catch (Throwable e) {
            throw new IllegalArgumentException("invalid probeModify");
        }
        //处理长轮询请求的入口方法
        configServletInner.doPollingConfig(request, response, clientMd5Map, probeModify.length());
    }


    @PostMapping("/remove/config/cache")
    public Result removeConfigCache(@RequestBody Map<String, String> bodyMap) {
        //暂时不实现
        return Results.success();
    }


    //这个方法就是注册客户端线程池信息到服务端的入口方法
    @PostMapping("/register")
    public Result register(@RequestBody DynamicThreadPoolRegisterWrapper registerWrapper) {
        configService.register(registerWrapper);
        return Results.success();
    }
}
