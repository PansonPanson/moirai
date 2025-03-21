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

package top.panson.springboot.start.core;


import top.panson.common.api.ClientCloseHookExecute;
import top.panson.common.constant.Constants;
import top.panson.common.design.builder.ThreadFactoryBuilder;
import top.panson.common.model.InstanceInfo;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.common.web.exception.ErrorCodeEnum;
import top.panson.springboot.start.remote.HttpAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static top.panson.common.constant.Constants.BASE_PATH;




/**
 * @方法描述：服务发现客户端，这个客户端会把客户端封装成一个服务实例对象注册到服务端
 */
@Slf4j
public class DiscoveryClient implements DisposableBean {

    //执行客户端与服务端心跳检测的定时任务执行器，注意，这里的这个心跳检测和ServerHealthCheck健康检查还不一样
    //健康检查是客户端主动检测服务端状况，而心跳检测实际上是让服务端知道客户端仍然存活，这个作用大家要区分一下
    private final ScheduledExecutorService scheduler;

    private final HttpAgent httpAgent;

    //服务实例对象
    private final InstanceInfo instanceInfo;

    //上一次给服务端发送心跳请求的时间
    private volatile long lastSuccessfulHeartbeatTimestamp = -1;

    //日志前缀
    private static final String PREFIX = "DiscoveryClient_";

    //服务实例的标识符
    private final String appPathIdentifier;

    //构造方法
    public DiscoveryClient(HttpAgent httpAgent, InstanceInfo instanceInfo) {
        this.httpAgent = httpAgent;
        //在这里给服务实例对象赋值了
        this.instanceInfo = instanceInfo;
        this.appPathIdentifier = instanceInfo.getAppName().toUpperCase() + "/" + instanceInfo.getInstanceId();
        //在这里创建了定时任务执行器
        this.scheduler = new ScheduledThreadPoolExecutor(
                new Integer(1),
                ThreadFactoryBuilder.builder().daemon(true).prefix("client.discovery.scheduler").build());
        //把服务实例注册到服务端
        register();
        //初始化定时任务
        initScheduledTasks();
    }

    //初始化定时任务的方法
    private void initScheduledTasks() {
        //每30秒执行一次HeartbeatThread心跳任务
        scheduler.scheduleWithFixedDelay(new HeartbeatThread(), 30, 30, TimeUnit.SECONDS);
    }


    /**
     * @方法描述：把服务实例注册到服务端的方法
     */
    boolean register() {
        log.info("{}{} - registering service...", PREFIX, appPathIdentifier);
        //得到服务端的请求路径
        String urlPath = BASE_PATH + "/apps/register/";
        Result registerResult;
        try {
            //把服务实例注册到服务端
            registerResult = httpAgent.httpPostByDiscovery(urlPath, instanceInfo);
        } catch (Exception ex) {
            registerResult = Results.failure(ErrorCodeEnum.SERVICE_ERROR);
            log.error("{}{} - registration failed: {}", PREFIX, appPathIdentifier, ex.getMessage());
        }
        if (log.isInfoEnabled()) {
            log.info("{}{} - registration status: {}", PREFIX, appPathIdentifier, registerResult.isSuccess() ? "success" : "fail");
        }
        return registerResult.isSuccess();
    }


    /**
     * @方法描述：当客户端要关闭时会执行这个方法
     */
    @Override
    public void destroy() throws Exception {
        log.info("{}{} - destroy service...", PREFIX, appPathIdentifier);
        String clientCloseUrlPath = Constants.BASE_PATH + "/client/close";
        Result clientCloseResult;
        try {
            //得到服务实例的groupKeyIp
            String groupKeyIp = new StringBuilder()
                    .append(instanceInfo.getGroupKey())
                    .append(Constants.GROUP_KEY_DELIMITER)
                    .append(instanceInfo.getIdentify())
                    .toString();
            //创建客户端关闭请求，这个请求会发送给服务端
            ClientCloseHookExecute.ClientCloseHookReq clientCloseHookReq = new ClientCloseHookExecute.ClientCloseHookReq();
            //给请求的成员变量赋值
            clientCloseHookReq.setAppName(instanceInfo.getAppName())
                    .setInstanceId(instanceInfo.getInstanceId())
                    .setGroupKey(groupKeyIp);
            //给服务端发送请求，通知服务端这个服务实例要下线了
            clientCloseResult = httpAgent.httpPostByDiscovery(clientCloseUrlPath, clientCloseHookReq);
            if (clientCloseResult.isSuccess()) {
                log.info("{}{} -client close hook success.", PREFIX, appPathIdentifier);
            }
        } catch (Throwable ex) {
            if (ex instanceof ShutdownExecuteException) {
                return;
            }
            log.error("{}{} - client close hook fail.", PREFIX, appPathIdentifier, ex);
        }
    }


    //心跳检测方法
    public class HeartbeatThread implements Runnable {

        @Override
        public void run() {
            //这里我要解释一下，客户端服务实例和服务端的心跳实际上是靠一个租约来维持的
            //客户端的服务实例注册到服务端之后，会创建一个对应的租约对象，客户端心跳实际上就是定期续约
            //如果超过一定时间没有续约，服务端就会认为这个服务实例下线了，就会移除这个服务实例
            //renew就是续约的方法
            if (renew()) {
                //续约之后更新lastSuccessfulHeartbeatTimestamp的值
                lastSuccessfulHeartbeatTimestamp = System.currentTimeMillis();
            }
        }
    }


    /**
     * @方法描述：服务实例向服务端续约的方法
     */
    private boolean renew() {
        Result renewResult;
        try {
            //创建服务实例续约对象，这个对象中封装着服务实例关键信息
            InstanceInfo.InstanceRenew instanceRenew = new InstanceInfo.InstanceRenew()
                    .setAppName(instanceInfo.getAppName())
                    .setInstanceId(instanceInfo.getInstanceId())
                    .setLastDirtyTimestamp(instanceInfo.getLastDirtyTimestamp().toString())
                    .setStatus(instanceInfo.getStatus().toString());
            //发送续约请求给服务端
            renewResult = httpAgent.httpPostByDiscovery(BASE_PATH + "/apps/renew", instanceRenew);
            //这里是判断服务端是否根本就没找到这个服务实例
            if (Objects.equals(ErrorCodeEnum.NOT_FOUND.getCode(), renewResult.getCode())) {
                //如果没找到就把更新当前服务实例的最新你修改时间
                //并设置当前服务实例信息为脏信息
                long timestamp = instanceInfo.setIsDirtyWithTime();
                //重新把当前服务实例注册到服务端
                boolean success = register();
                if (success) {
                    //注册成功则清除服务信息的脏标志
                    instanceInfo.unsetIsDirty(timestamp);
                }
                return success;
            }
            return renewResult.isSuccess();
        } catch (Exception ex) {
            log.error(PREFIX + "{} - was unable to send heartbeat!", appPathIdentifier, ex);
            return false;
        }
    }
}
