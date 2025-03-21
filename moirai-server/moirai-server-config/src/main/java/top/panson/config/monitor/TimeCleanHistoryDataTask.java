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

package top.panson.config.monitor;

import top.panson.common.executor.ExecutorFactory;
import top.panson.common.toolkit.DateUtil;
import top.panson.config.config.ServerBootstrapProperties;
import top.panson.config.model.HisRunDataInfo;
import top.panson.config.service.biz.HisRunDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static top.panson.common.constant.Constants.DEFAULT_GROUP;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：定期清理线程池历史信息的任务
 */
@Component
@RequiredArgsConstructor
public class TimeCleanHistoryDataTask implements Runnable, InitializingBean {

    @NonNull
    private final ServerBootstrapProperties properties;

    //客户端上报的线程池运行信息都会被这个对象存放到数据库里
    @NonNull
    private final HisRunDataService hisRunDataService;

    //定时任务执行器
    private ScheduledExecutorService cleanHistoryDataExecutor;

    //任务要执行的操作
    @Override
    public void run() {
        //properties.getCleanHistoryDataPeriod()这个得到的是线程池历史数据的清除周期，这里得到的就是30分钟清除一次
        //当前时间减去这个周期时间，如果数据库中线程运行信息数据的创建时间小于offsetMinuteDateTime，就意味着数据过期了，需要从数据库中移除
        LocalDateTime offsetMinuteDateTime = LocalDateTime.now().plusMinutes(-properties.getCleanHistoryDataPeriod());
        //构造查询条件，举个很直接的例子，比如当前时间是9点，减去30分钟，就是8.30，那么创建时间小于8.30的数据就应该被清楚了
        //其实看到这里，这么说更准确一点，不应该把properties.getCleanHistoryDataPeriod()称为清除周期，并不是说每隔30分钟就会清除一次
        //而是每次清除都会把存在了30分钟的历史信息清除掉，也就是说，在数据库中，线程池的历史信息只保存30分钟
        LambdaQueryWrapper<HisRunDataInfo> queryWrapper = Wrappers.lambdaQuery(HisRunDataInfo.class)
                .le(HisRunDataInfo::getTimestamp, DateUtil.getTime(offsetMinuteDateTime));
        hisRunDataService.remove(queryWrapper);
    }


    //InitializingBean接口中的方法，该方法被回调的时候，就会把当前对象当成任务提交给定时任务执行器
    @Override
    public void afterPropertiesSet() throws Exception {
        //判断是否开启了历史信息清除功能
        if (properties.getCleanHistoryDataEnable()) {
            //创建定时任务执行器
            cleanHistoryDataExecutor = ExecutorFactory.Managed
                    .newSingleScheduledExecutorService(DEFAULT_GROUP, r -> new Thread(r, "clean-history-data"));
            //提交定时任务
            cleanHistoryDataExecutor.scheduleWithFixedDelay(this, 0, 1, TimeUnit.MINUTES);
        }
    }
}
