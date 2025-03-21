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

package top.panson.config.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static top.panson.common.constant.Constants.WEIGHT_CONFIGS;

/**
 *
 * @方法描述：处理长轮询请求的类
 */
@Service
@RequiredArgsConstructor
public class ConfigServletInner {

    //长轮询服务器
    private final LongPollingService longPollingService;

    private static final int CLIENT_IDENTIFY_MAXIMUM_SIZE = 16384;

    //定义一个map，这个map中存放的key就是客户端的标识符
    private final Cache<String, Long> deWeightCache = Caffeine.newBuilder()
            .maximumSize(CLIENT_IDENTIFY_MAXIMUM_SIZE)
            .build();


    public String doPollingConfig(HttpServletRequest request, HttpServletResponse response, Map<String, String> clientMd5Map, int probeRequestSize) {
        //如果支持长轮询请求，并且请求没有重复发送
        if (LongPollingService.isSupportLongPolling(request) && weightVerification(request)) {
            //就把请求交给长轮询服务对象处理
            longPollingService.addLongPollingClient(request, response, clientMd5Map, probeRequestSize);
            return HttpServletResponse.SC_OK + "";
        }
        return HttpServletResponse.SC_OK + "";
    }


    //检查长轮询请求是否重复的方法
    private boolean weightVerification(HttpServletRequest request) {
        //得到客户端的唯一标识符
        String clientIdentify = request.getParameter(WEIGHT_CONFIGS);
        //判断map中是否存在对应的标识符了
        Long timeVal = deWeightCache.getIfPresent(clientIdentify);
        if (timeVal == null) {
            //如果不存在就把当前时间戳放到map中
            deWeightCache.put(clientIdentify, System.currentTimeMillis());
            return true;
        }//走到这里意味着map中存在标识符，说明请求重复了，直接返回false即可
        return false;
    }
}