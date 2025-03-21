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

package top.panson.auth.config;

import top.panson.auth.filter.RewriteUserInfoApiFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author:B站UP主陈清风扬，从零带你写框架系列教程的作者，个人微信号：chenqingfengyangjj。
 * @Description:系列教程目前包括手写Netty，XXL-JOB，Spring，RocketMq，Javac，JVM等课程。
 * @Date:2024/4/29
 * @Description:过滤器配置类，在这里我跟大家解释一下，这个auth模块的类我就不添加注释了，这一块就是非常普通的逻辑
 * 和用户登陆有关的，鉴权什么的，jwt那一套，这个大家肯定都特别熟悉了，我也就不再费精力添加这一块的注释了，希望大家理解一下
 * 因为这个框架使用的是mybatisplus，大家直接去对应的impl类下查看查找数据库的逻辑即可
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RewriteUserInfoApiFilter> userInfoApiFilterRegistrationBean() {
        FilterRegistrationBean<RewriteUserInfoApiFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RewriteUserInfoApiFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}