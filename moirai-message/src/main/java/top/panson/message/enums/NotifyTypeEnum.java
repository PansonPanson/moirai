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

package top.panson.message.enums;

//通知类型枚举类
public enum NotifyTypeEnum {

    //配置变更
    CHANGE,

    //队列容量使用率通知
    CAPACITY,

    //线程池负载通知
    ACTIVITY,

   //触发拒绝任务通知
    REJECT,

    //任务执行超时通知
    TIMEOUT
}
