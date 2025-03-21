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

package top.panson.springboot.start.provider;

import top.panson.common.api.ClientNetworkService;
import top.panson.common.model.InstanceInfo;
import top.panson.common.spi.DynamicThreadPoolServiceLoader;
import top.panson.common.toolkit.ContentUtil;
import top.panson.core.toolkit.IdentifyUtil;
import top.panson.core.toolkit.inet.InetUtils;
import top.panson.springboot.start.config.BootstrapProperties;
import top.panson.springboot.start.toolkit.CloudCommonIdUtil;
import lombok.SneakyThrows;
import org.springframework.core.env.ConfigurableEnvironment;
import java.net.InetAddress;
import java.util.Optional;
import static top.panson.common.constant.Constants.IDENTIFY_SLICER_SYMBOL;
import static top.panson.core.toolkit.IdentifyUtil.CLIENT_IDENTIFICATION_VALUE;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/6
 * @方法描述：创建服务实例对象的工厂
 */
public final class InstanceInfoProviderFactory {

    //在静态代码块中注册ClientNetworkService
    //这个ClientNetworkService接口的实现类是CustomerClientNetworkService
    //而这个CustomerClientNetworkService对象的作用就是用来提供服务端的IP地址和端口号的
    static {
        DynamicThreadPoolServiceLoader.register(ClientNetworkService.class);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/6
     * @方法描述：得到客户端服务实例对象的方法
     */
    @SneakyThrows
    public static InstanceInfo getInstance(final ConfigurableEnvironment environment,
                                           final BootstrapProperties bootstrapProperties,
                                           final InetUtils inetUtils) {
        //从配置文件中得到命名空间
        String namespace = bootstrapProperties.getNamespace();
        //得到项目ID
        String itemId = bootstrapProperties.getItemId();
        //得到客户端使用的端口号
        String port = environment.getProperty("server.port", "8080");
        //得到动态线程池的item-id，这个item-id和上面的itemId一样
        String applicationName = environment.getProperty("spring.dynamic.thread-pool.item-id");
        //得到springboot使用的配置文件，也就是激活的配置文件
        String active = environment.getProperty("spring.profiles.active", "UNKNOWN");
        //创建一个InstanceInfo对象
        InstanceInfo instanceInfo = new InstanceInfo();
        //创建一个实例ID
        String instanceId = CloudCommonIdUtil.getDefaultInstanceId(environment, inetUtils);
        //为实例ID拼接UUID，得到完整的实例ID
        instanceId = new StringBuilder()
                .append(instanceId).append(IDENTIFY_SLICER_SYMBOL).append(CLIENT_IDENTIFICATION_VALUE).toString();
        //从配置文件中得到上下文路径
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        //在这里给创建出来的InstanceInfo对象的各个成员变量赋值
        instanceInfo.setInstanceId(instanceId)
                .setIpApplicationName(CloudCommonIdUtil.getIpApplicationName(environment, inetUtils))
                .setHostName(InetAddress.getLocalHost().getHostAddress()).setAppName(applicationName)
                .setPort(port).setClientBasePath(contextPath).setGroupKey(ContentUtil.getGroupKey(itemId, namespace));
        //接下来就是设置客户端回调地址的操作了，使用在静态代码块中注册的ClientNetworkService接口的实现类对象得到客户端的IP地址和端口号
        String[] customerNetwork = DynamicThreadPoolServiceLoader.getSingletonServiceInstances(ClientNetworkService.class)
                .stream().findFirst().map(each -> each.getNetworkIpPort(environment)).orElse(null);
        //这里得到了客户端的IP地址和端口号，拼接成一个完整的地址，这里我想多解释一下，在这个动态线程池框架中
        //实际上只有用户自己创建的动态线程池可以被注册到服务端
        //其他第三方框架，比如dubbo，rocketmq，或者是web容器的线程池，也就是tomcat线程池，这些线程池的信息是没有存放到服务端的数据库中的
        //这些第三方线程池的运行时信息都是由客户端自己监控自己收集，如果用户在web界面向直到tomcat线程池或者是dubbo线程池的信息，那么就会先通过web界面
        //访问后端，后端再根据InstanceInfo提供的callBackUrl地址来访问客户端，从客户端获得第三方线程池的信息返回给服务端，再返回给前端web界面，这个流程大家要理清楚
        //那服务端怎么知道客户端的地址呢？这里设置的这个客户端回调地址，就是用来让服务端给客户端主动发送请求使用的
        //但是在这个框架中，客户端的回调地址硬编码为127.0.0.1了，也就是说服务端和客户端在同一台服务器上，服务器才能成功访问客户端
        String callBackUrl = new StringBuilder().append(Optional.ofNullable(customerNetwork).map(each -> each[0]).orElse(instanceInfo.getHostName())).append(":")
                .append(Optional.ofNullable(customerNetwork).map(each -> each[1]).orElse(port)).append(instanceInfo.getClientBasePath())
                .toString();
        //设置客户端的回调地址
        instanceInfo.setCallBackUrl(callBackUrl);
        //得到服务实例的标识，这个标识是要在前端展示给用户的
        //这个其实就是客户端的ip地址+端口号+uuid
        String identify = IdentifyUtil.generate(environment, inetUtils);
        instanceInfo.setIdentify(identify);
        //设置激活的配置文件
        instanceInfo.setActive(active.toUpperCase());
        return instanceInfo;
    }
}
