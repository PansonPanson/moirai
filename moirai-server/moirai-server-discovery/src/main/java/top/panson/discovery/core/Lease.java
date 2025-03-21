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

package top.panson.discovery.core;

import lombok.Getter;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/6
 * @方法描述：租约对象
 */
public class Lease<T> {

    //这里就会被客户端注册的服务实例对象赋值
    private T holder;

    //服务实例要被清除的时间，这个时间默认为0.除非调用了本类的cancel方法
    //才会用当前时间给这个成员变量赋值，否则服务实例不会被清除，除非客户端没有及时续约
    @Getter
    private long evictionTimestamp;

    //租约对象的创建时间
    @Getter
    private long registrationTimestamp;

    //服务实例第一次的上线时间
    private long serviceUpTimestamp;

    //服务实例最新的更新时间，客户端每一次续约，这个时间都会被更新
    @Getter
    private volatile long lastUpdateTimestamp;

    //服务实例的租约时间
    private long duration;

    //默认的租约时间，90秒
    public static final int DEFAULT_DURATION_IN_SECS = 90;

    //构造方法
    public Lease(T r) {
        holder = r;
        registrationTimestamp = System.currentTimeMillis();
        lastUpdateTimestamp = registrationTimestamp;
        duration = DEFAULT_DURATION_IN_SECS * 1000;
    }

    //续约方法
    public void renew() {
        //在这里可以看到，每一次续约，都会更新lastUpdateTimestamp的值
        lastUpdateTimestamp = System.currentTimeMillis() + duration;
    }

    //取消租约的方法
    public void cancel() {
        if (evictionTimestamp <= 0) {
            evictionTimestamp = System.currentTimeMillis();
        }
    }

    //设置服务实例上线时间的方法
    public void serviceUp() {
        if (serviceUpTimestamp == 0) {
            serviceUpTimestamp = System.currentTimeMillis();
        }
    }

    //设置服务实例上线时间
    public void setServiceUpTimestamp(long serviceUpTimestamp) {
        this.serviceUpTimestamp = serviceUpTimestamp;
    }

    //判断服务实例是否过期的方法
    public boolean isExpired() {
        return isExpired(0L);
    }


    //判断服务实例是否过期的方法
    public boolean isExpired(long additionalLeaseMs) {
        return (evictionTimestamp > 0 || System.currentTimeMillis() > (lastUpdateTimestamp + additionalLeaseMs));
    }


    public long getServiceUpTimestamp() {
        return serviceUpTimestamp;
    }


    public T getHolder() {
        return holder;
    }
}