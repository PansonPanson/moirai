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

import top.panson.common.constant.Constants;
import top.panson.common.model.InstanceInfo;
import top.panson.common.toolkit.BeanUtil;
import top.panson.common.toolkit.CollectionUtil;
import top.panson.common.toolkit.StringUtil;
import top.panson.common.toolkit.http.HttpUtil;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.config.model.CacheItem;
import top.panson.config.model.biz.threadpool.ThreadPoolDelReqDTO;
import top.panson.config.model.biz.threadpool.ThreadPoolQueryReqDTO;
import top.panson.config.model.biz.threadpool.ThreadPoolRespDTO;
import top.panson.config.model.biz.threadpool.ThreadPoolSaveOrUpdateReqDTO;
import top.panson.config.service.ConfigCacheService;
import top.panson.config.service.biz.ThreadPoolService;
import top.panson.console.model.ThreadPoolInstanceInfo;
import top.panson.console.model.WebThreadPoolReqDTO;
import top.panson.console.model.WebThreadPoolRespDTO;
import top.panson.discovery.core.BaseInstanceRegistry;
import top.panson.discovery.core.Lease;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static top.panson.common.toolkit.ContentUtil.getGroupKey;

/**
 * @author:B站UP主陈清风扬，从零带你写框架系列教程的作者，个人微信号：chenqingfengyangjj。
 * @Description:系列教程目前包括手写Netty，XXL-JOB，Spring，RocketMq，Javac，JVM等课程。
 * @Date:2024/4/29
 * @Description:这个控制器就是第一版本最核心的一个控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.BASE_PATH + "/thread/pool")
public class ThreadPoolController {

    //线程池服务类，这个服务类中封装了去数据库中查询线程池信息的操作
    private final ThreadPoolService threadPoolService;

    //服务发现组件
    private final BaseInstanceRegistry baseInstanceRegistry;

    private static final String HTTP = "http://";

    //在第一版本中，我们只会使用这个方法处理前端发送过来的查询线程池信息的请求
    @PostMapping("/query/page")
    public Result<IPage<ThreadPoolRespDTO>> queryNameSpacePage(@RequestBody ThreadPoolQueryReqDTO reqDTO) {
        return Results.success(threadPoolService.queryThreadPoolPage(reqDTO));
    }


    @PostMapping("/query")
    public Result<ThreadPoolRespDTO> queryNameSpace(@RequestBody ThreadPoolQueryReqDTO reqDTO) {
        return Results.success(threadPoolService.getThreadPool(reqDTO));
    }

    //调用这个方法时，对应的是web界面线程池管理中的修改操作
    //identify就是实例标识，这个修改操作并不会让服务端的事件通知中心发布服务变更事件
    @PostMapping("/save_or_update")
    public Result saveOrUpdateThreadPoolConfig(@RequestParam(value = "identify", required = false) String identify,
                                               @Validated @RequestBody ThreadPoolSaveOrUpdateReqDTO reqDTO) {
        threadPoolService.saveOrUpdateThreadPoolConfig(identify, reqDTO);
        return Results.success();
    }

    @DeleteMapping("/delete")
    public Result deletePool(@RequestBody ThreadPoolDelReqDTO reqDTO) {

        return Results.success();
    }

    @PostMapping("/alarm/enable/{id}/{isAlarm}")
    public Result alarmEnable(@PathVariable("id") String id, @PathVariable("isAlarm") Integer isAlarm) {
        threadPoolService.alarmEnable(id, isAlarm);
        return Results.success();
    }

    //查看动态线程池实时信息的方法
    //这个方法和下面的方法的逻辑就会跑到WebThreadPoolRunStateController类中，对的，没错就是又来到客户端中了
    //原因很简单，因为线程池本身就是在部署客户端的服务实例中运行的，当然应该从客户端那一边收集信息
    @GetMapping("/run/state/{tpId}")
    public Result runState(@PathVariable("tpId") String tpId,
                           @RequestParam(value = "clientAddress") String clientAddress) {
        String urlString = StringUtil.newBuilder(HTTP, clientAddress, "/run/state/", tpId);
        return HttpUtil.get(urlString, Result.class);
    }

    //查看动态线程池线程信息的方法
    @GetMapping("/run/thread/state/{tpId}")
    public Result runThreadState(@PathVariable("tpId") String tpId,
                                 @RequestParam(value = "clientAddress") String clientAddress) {
        String urlString = StringUtil.newBuilder(HTTP, clientAddress, "/run/thread/state/", tpId);
        return HttpUtil.get(urlString, Result.class);
    }

    //web界面web容器线程池信息，itemId就是线程池的名称，比如消费线程池，还是生产者线程池
    //这个方法的大概逻辑和本类最后一个方法，也就是查询动态线程池信息的逻辑基本类似，只不过这个方法要去客户端查询web容器的线程池信息
    //因为web容器线程池的信息不会注册到服务端
    @GetMapping("/list/client/instance/{itemId}")
    public Result listClientInstance(@PathVariable("itemId") String itemId,
                                     @RequestParam(value = "mark", required = false) String mark) {
        List<Lease<InstanceInfo>> leases = baseInstanceRegistry.listInstance(itemId);
        Lease<InstanceInfo> first = CollectionUtil.getFirst(leases);
        if (first == null) {
            return Results.success(new ArrayList<>());
        }
        List<WebThreadPoolRespDTO> returnThreadPool = new ArrayList<>();
        for (Lease<InstanceInfo> each : leases) {
            Result poolBaseState;
            try {//在这里去访问客户端，获得web容器线程池信息
                poolBaseState = getPoolBaseState(mark, each.getHolder().getCallBackUrl());
            } catch (Throwable ignored) {
                continue;
            }
            Object data = poolBaseState.getData();
            if (data == null) {
                continue;
            }
            WebThreadPoolRespDTO result = BeanUtil.convert(data, WebThreadPoolRespDTO.class);
            result.setItemId(itemId);
            result.setTenantId(each.getHolder().getGroupKey().split("[+]")[1]);
            result.setActive(each.getHolder().getActive());
            result.setIdentify(each.getHolder().getIdentify());
            result.setClientAddress(each.getHolder().getCallBackUrl());
            returnThreadPool.add(result);
        }
        return Results.success(returnThreadPool);
    }


    //web界面web容器线程池信息
    @GetMapping("/web/base/info")
    public Result getPoolBaseState(@RequestParam(value = "mark") String mark,
                                   @RequestParam(value = "clientAddress") String clientAddress) {
        String urlString = StringUtil.newBuilder(HTTP, clientAddress, "/web/base/info", "?mark=", mark);
        return HttpUtil.get(urlString, Result.class);
    }

    //web界面web容器线程池信息
    @GetMapping("/web/run/state")
    public Result getPoolRunState(@RequestParam(value = "clientAddress") String clientAddress) {
        String urlString = StringUtil.newBuilder(HTTP, clientAddress, "/web/run/state");
        return HttpUtil.get(urlString, Result.class);
    }

    //web界面web容器线程池更新处理方法
    @PostMapping("/web/update/pool")
    public Result<Void> updateWebThreadPool(@RequestBody WebThreadPoolReqDTO requestParam) {
        for (String each : requestParam.getClientAddressList()) {
            String urlString = StringUtil.newBuilder(HTTP, each, "/web/update/pool");
            HttpUtil.post(urlString, requestParam);
        }
        return Results.success();
    }



    //web界面动态线程池的线程池实例的信息，itemId就是项目Id，tpId就是线程池的名称
    @GetMapping("/list/instance/{itemId}/{tpId}")
    public Result<List<ThreadPoolInstanceInfo>> listInstance(@PathVariable("itemId") String itemId,
                                                             @PathVariable("tpId") String tpId) {

        //从服务发现注册表中得到对应的服务实例信息，这里得到的是一个租约集合
        List<Lease<InstanceInfo>> leases = baseInstanceRegistry.listInstance(itemId);
        //如果第一个数据为空，说明集合中没有数据，直接返回即可
        Lease<InstanceInfo> first = CollectionUtil.getFirst(leases);
        if (first == null) {
            return Results.success(new ArrayList<>());
        }
        //得到服务实例信息
        InstanceInfo holder = first.getHolder();
        String itemTenantKey = holder.getGroupKey();
        //构造一个组合键
        String groupKey = getGroupKey(tpId, itemTenantKey);
        //通过组合键先去服务缓存对象中获得缓存的服务信息
        Map<String, CacheItem> content = ConfigCacheService.getContent(groupKey);
        //得到所有激活的服务实例map，最后这个activeMap中的key就是服务实例的标识符，value就是激活的配置文件名称
        Map<String, String> activeMap =
                leases.stream().map(Lease::getHolder).filter(each -> StringUtil.isNotBlank(each.getActive()))
                        .collect(Collectors.toMap(InstanceInfo::getIdentify, InstanceInfo::getActive));
        //这里得到的map就是服务实例客户端的基础路径map，基础路径其实就是那个/example
        Map<String, String> clientBasePathMap = leases.stream().map(Lease::getHolder)
                .filter(each -> StringUtil.isNotBlank(each.getClientBasePath()))
                .collect(Collectors.toMap(InstanceInfo::getIdentify, InstanceInfo::getClientBasePath));
        //定义返回前端的线程池服务实例列表，注意，这里返回的是线程池服务实例信息，也就是线程池本身的信息和线程池所属服务实例的信息都会返回
        List<ThreadPoolInstanceInfo> returnThreadPool = new ArrayList<>();
        //填充returnThreadPool集合
        content.forEach((key, val) -> {
            ThreadPoolInstanceInfo threadPoolInstanceInfo =
                    BeanUtil.convert(val.configAllInfo, ThreadPoolInstanceInfo.class);
            //得到线程池所在客户端地址
            threadPoolInstanceInfo.setClientAddress(StringUtil.subBefore(key, Constants.IDENTIFY_SLICER_SYMBOL));
            //设置服务实例激活状态
            threadPoolInstanceInfo.setActive(activeMap.get(key));
            //设置服务实例唯一标识
            threadPoolInstanceInfo.setIdentify(key);
            //设置客户端基础路径
            threadPoolInstanceInfo.setClientBasePath(clientBasePathMap.get(key));
            returnThreadPool.add(threadPoolInstanceInfo);
        });
        //返回结果给前端
        return Results.success(returnThreadPool);
    }
}
