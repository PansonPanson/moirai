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

package top.panson.message.service;

import top.panson.common.config.ApplicationContextHolder;
import top.panson.common.toolkit.CollectionUtil;
import top.panson.message.api.NotifyConfigBuilder;
import top.panson.message.dto.AlarmControlDTO;
import top.panson.message.dto.NotifyConfigDTO;
import top.panson.message.enums.NotifyTypeEnum;
import top.panson.message.request.AlarmNotifyRequest;
import top.panson.message.request.ChangeParameterNotifyRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @方法描述：提供了发送告警信息或者配置变更信息基础服务的类，这个类实现了CommandLineRunner接口
 */
@Slf4j
@RequiredArgsConstructor
public class Hippo4jBaseSendMessageService implements Hippo4jSendMessageService, CommandLineRunner {

    private final NotifyConfigBuilder notifyConfigBuilder;

    private final AlarmControlHandler alarmControlHandler;

    //存放线程池对应的通知配置信息的map
    @Getter
    public final Map<String, List<NotifyConfigDTO>> notifyConfigs = new HashMap<>();


    //存放消息发送器的map，这里的消息发送器就是每个平台消息发送的对象，对应着微信等等平台
    private final Map<String, SendMessageHandler> sendMessageHandlers = new HashMap<>();


    /**
     * @方法描述：向用户发送告警通知的方法，方法的逻辑很简单，我就不添加注释了
     */
    @Override
    public void sendAlarmMessage(NotifyTypeEnum typeEnum, AlarmNotifyRequest alarmNotifyRequest) {
        String threadPoolId = alarmNotifyRequest.getThreadPoolId();
        String buildKey = new StringBuilder()
                .append(threadPoolId)
                .append("+")
                .append("ALARM")
                .toString();
        List<NotifyConfigDTO> notifyList = notifyConfigs.get(buildKey);
        if (CollectionUtil.isEmpty(notifyList)) {
            return;
        }
        notifyList.forEach(each -> {
            try {
                SendMessageHandler messageHandler = sendMessageHandlers.get(each.getPlatform());
                if (messageHandler == null) {
                    log.warn("Please configure alarm notification on the server. key: [{}]", threadPoolId);
                    return;
                }
                if (isSendAlarm(each.getTpId(), each.getPlatform(), typeEnum)) {
                    alarmNotifyRequest.setNotifyTypeEnum(typeEnum);
                    messageHandler.sendAlarmMessage(each, alarmNotifyRequest);
                }
            } catch (Exception ex) {
                log.warn("Failed to send thread pool alarm notification. key: [{}]", threadPoolId, ex);
            }
        });
    }


    /**
     * @方法描述：向用户发送配置变更通知的方法，方法的逻辑很简单，我就不添加注释了
     */
    @Override
    public void sendChangeMessage(ChangeParameterNotifyRequest changeParameterNotifyRequest) {
        String threadPoolId = changeParameterNotifyRequest.getThreadPoolId();
        String buildKey = new StringBuilder()
                .append(threadPoolId)
                .append("+")
                .append("CONFIG")
                .toString();
        List<NotifyConfigDTO> notifyList = notifyConfigs.get(buildKey);
        if (CollectionUtil.isEmpty(notifyList)) {
            log.warn("Please configure alarm notification on the server. key: [{}]", threadPoolId);
            return;
        }
        notifyList.forEach(each -> {
            try {
                SendMessageHandler messageHandler = sendMessageHandlers.get(each.getPlatform());
                if (messageHandler == null) {
                    log.warn("Please configure alarm notification on the server. key: [{}]", threadPoolId);
                    return;
                }
                messageHandler.sendChangeMessage(each, changeParameterNotifyRequest);
            } catch (Exception ex) {
                log.warn("Failed to send thread pool change notification. key: [{}]", threadPoolId, ex);
            }
        });
    }


    //判断是否可以向用户发送告警通知的方法
    private boolean isSendAlarm(String threadPoolId, String platform, NotifyTypeEnum typeEnum) {
        AlarmControlDTO alarmControl = AlarmControlDTO.builder()
                .threadPool(threadPoolId)
                .platform(platform)
                .typeEnum(typeEnum)
                .build();
        return alarmControlHandler.isSendAlarm(alarmControl);
    }




    public synchronized void putPlatform(Map<String, List<NotifyConfigDTO>> notifyConfigs) {
        this.notifyConfigs.putAll(notifyConfigs);
    }



    /**
     * @方法描述：CommandLineRunner接口中的回调方法，这个接口回调的时候，会访问服务端，从服务端获取线程池对应的通知告警配置信息
     */
    @Override
    public void run(String... args) throws Exception {
        //在这里从Springboot容器中获得所有的告警通知消息发送器
        Map<String, SendMessageHandler> sendMessageHandlerMap =
                ApplicationContextHolder.getBeansOfType(SendMessageHandler.class);
        //把消息发送器存放到map中
        sendMessageHandlerMap.values().forEach(each -> sendMessageHandlers.put(each.getType(), each));
        //在这里使用配置信息构建器从服务器获取线程池对应的通知配置信息
        Map<String, List<NotifyConfigDTO>> buildNotify = notifyConfigBuilder.buildNotify();
        //缓存到map中
        notifyConfigs.putAll(buildNotify);
    }
}
