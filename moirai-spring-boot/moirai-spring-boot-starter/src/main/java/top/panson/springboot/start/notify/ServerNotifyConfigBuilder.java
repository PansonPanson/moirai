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

package top.panson.springboot.start.notify;

import top.panson.common.toolkit.CollectionUtil;
import top.panson.common.toolkit.GroupKey;
import top.panson.common.toolkit.JSONUtil;
import top.panson.common.web.base.Result;
import top.panson.core.executor.manage.GlobalThreadPoolManage;
import top.panson.message.api.NotifyConfigBuilder;
import top.panson.message.dto.NotifyConfigDTO;
import top.panson.message.dto.ThreadPoolNotifyDTO;
import top.panson.message.request.ThreadPoolNotifyRequest;
import top.panson.message.service.AlarmControlHandler;
import top.panson.springboot.start.config.BootstrapProperties;
import top.panson.springboot.start.remote.HttpAgent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static top.panson.common.constant.Constants.BASE_PATH;


/**
 * @方法描述：这个类就是用来构建客户端线程池的通知告警配置信息的，这些信息要从服务器获取
 */
@Slf4j
@AllArgsConstructor
public class ServerNotifyConfigBuilder implements NotifyConfigBuilder {

    private final HttpAgent httpAgent;

    private final BootstrapProperties properties;

    //告警控制器
    private final AlarmControlHandler alarmControlHandler;


    //构建线程池通知告警配置信息的入口方法
    @Override
    public Map<String, List<NotifyConfigDTO>> buildNotify() {
        //从全局线程池管理器中得到线程池的所有Id
        List<String> threadPoolIds = GlobalThreadPoolManage.listThreadPoolId();
        //判空操作
        if (CollectionUtil.isEmpty(threadPoolIds)) {
            log.warn("The client does not have a dynamic thread pool instance configured.");
            return new HashMap<>();
        }
        //在这里获得线程池对应的告警通知配置信息
        return getAndInitNotify(threadPoolIds);
    }



    /**
     * @方法描述：访问服务端，获得线程池对应的告警通知配置信息
     */
    public Map<String, List<NotifyConfigDTO>> getAndInitNotify(List<String> threadPoolIds) {
        Map<String, List<NotifyConfigDTO>> resultMap = new HashMap<>();
        List<String> groupKeys = new ArrayList<>();
        //遍历线程池的Id，用Id得到组合键，添加到groupKeys集合中
        threadPoolIds.forEach(each -> {
            String groupKey = GroupKey.getKeyTenant(each, properties.getItemId(), properties.getNamespace());
            groupKeys.add(groupKey);
        });
        Result result = null;
        try {
            //直接访问服务端，获取线程池通知的配置信息
            result = httpAgent.httpPostByDiscovery(BASE_PATH + "/notify/list/config", new ThreadPoolNotifyRequest(groupKeys));
        } catch (Throwable ex) {
            log.error("Get dynamic thread pool notify configuration error. message: {}", ex.getMessage());
        }
        //判断请求是否成功
        if (result != null && result.isSuccess() && result.getData() != null) {
            String resultDataStr = JSONUtil.toJSONString(result.getData());
            //将服务端返回的响应数据转换为resultData集合对象
            List<ThreadPoolNotifyDTO> resultData = JSONUtil.parseArray(resultDataStr, ThreadPoolNotifyDTO.class);
            //遍历集合，把信息缓存到resultMap中
            resultData.forEach(each -> resultMap.put(each.getNotifyKey(), each.getNotifyList()));
            //这里是判断线程池对应的通知配置信息是否存在ALARM类型的，也就是告警通知，如果存在就把线程池对应的告警通知的配置信息存放到alarmControlHandler对象的threadPoolAlarmCache中
            //注意，这里我多解释一下，在这个框架源码中，作者提供的测试类中，默认线程池通知告警功能是不开启的，如果是这样的话，那么客户端是无法从服务端获取任何通知告警的配置信息的
            //只有用户在配置文件设置开启了线程池的通知告警功能，才可以从服务端查询到通知告警配置信息
            resultMap.forEach((key, val) -> val.stream().filter(each -> Objects.equals("ALARM", each.getType()))
                    .forEach(each -> alarmControlHandler.initCacheAndLock(each.getTpId(), each.getPlatform(), each.getInterval())));
        }
        return resultMap;
    }
}
