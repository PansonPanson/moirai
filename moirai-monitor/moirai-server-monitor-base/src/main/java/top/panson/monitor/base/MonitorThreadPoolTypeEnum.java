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

package top.panson.monitor.base;

/**
 * Monitor thread-pool type enum.
 */
public enum MonitorThreadPoolTypeEnum {


    //收集动态线程池的运行信息
    DYNAMIC,

    //收集web线程池，也就是tomcat线程池运行信息
    WEB,

    //收集dubbo等线程池运行信息
    ADAPTER
}
