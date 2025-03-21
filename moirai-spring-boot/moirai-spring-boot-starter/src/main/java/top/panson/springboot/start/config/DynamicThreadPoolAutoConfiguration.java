package top.panson.springboot.start.config;


import top.panson.adapter.base.ThreadPoolAdapterBeanContainer;
import top.panson.common.api.ThreadDetailState;
import top.panson.common.api.ThreadPoolCheckAlarm;
import top.panson.common.api.ThreadPoolConfigChange;
import top.panson.common.api.ThreadPoolDynamicRefresh;
import top.panson.common.config.ApplicationContextHolder;
import top.panson.core.config.UtilAutoConfiguration;
import top.panson.core.enable.MarkerConfiguration;
import top.panson.core.executor.state.ThreadPoolRunStateHandler;
import top.panson.core.executor.support.service.DynamicThreadPoolService;
import top.panson.core.handler.DynamicThreadPoolBannerHandler;
import top.panson.core.toolkit.IdentifyUtil;
import top.panson.core.toolkit.inet.InetUtils;
import top.panson.message.api.NotifyConfigBuilder;
import top.panson.message.config.MessageConfiguration;
import top.panson.message.service.*;
import top.panson.springboot.start.core.BaseThreadDetailStateHandler;
import top.panson.springboot.start.core.ClientWorker;
import top.panson.springboot.start.core.DynamicThreadPoolSubscribeConfig;
import top.panson.springboot.start.core.ServerThreadPoolDynamicRefresh;
import top.panson.springboot.start.event.ApplicationContentPostProcessor;
import top.panson.springboot.start.monitor.ReportingEventExecutor;
import top.panson.springboot.start.monitor.collect.RunTimeInfoCollector;
import top.panson.springboot.start.monitor.send.MessageSender;
import top.panson.springboot.start.monitor.send.http.HttpConnectSender;
import top.panson.springboot.start.notify.ServerNotifyConfigBuilder;
import top.panson.springboot.start.remote.HttpAgent;
import top.panson.springboot.start.remote.HttpScheduledHealthCheck;
import top.panson.springboot.start.remote.ServerHealthCheck;
import top.panson.springboot.start.remote.ServerHttpAgent;
import top.panson.springboot.start.support.DynamicThreadPoolConfigService;
import top.panson.springboot.start.support.DynamicThreadPoolPostProcessor;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;




/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/26
 * @方法描述：这个就是spring-starter中的核心类，这个类使用了大量springboot的功能，这个类上的springboot注解应该都是常用注解了，所以这些功能我就不写注释了
 */
@Configuration
@AllArgsConstructor
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@EnableConfigurationProperties(BootstrapProperties.class)
@ConditionalOnProperty(prefix = BootstrapProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
@ImportAutoConfiguration({DiscoveryConfiguration.class, UtilAutoConfiguration.class, MessageConfiguration.class})
public class DynamicThreadPoolAutoConfiguration {

    //在这里把配置文件中的相关信息封封装到这个成员变量中了
    private final BootstrapProperties properties;

    //springboot的环境变量
    private final ConfigurableEnvironment environment;


    //创建控制台输出图案的对象，并且把它交给spring容器管理
    @Bean
    public DynamicThreadPoolBannerHandler threadPoolBannerHandler() {
        return new DynamicThreadPoolBannerHandler(properties);
    }

    //这里创建的这个hippo4JApplicationContextHolder对象其实就是对spring的Appliycation上下文对象做了一封包装
    //通过这个hippo4JApplicationContextHolder对象可以随时得到spring的Appliycation上下文对象了
    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ApplicationContextHolder hippo4JApplicationContextHolder() {
        return new ApplicationContextHolder();
    }


    //创建动态线程池的服务对象
    @Bean
    @SuppressWarnings("all")
    public DynamicThreadPoolService dynamicThreadPoolConfigService(HttpAgent httpAgent,
                                                                   ServerHealthCheck serverHealthCheck,
                                                                   ServerNotifyConfigBuilder notifyConfigBuilder,
                                                                   Hippo4jBaseSendMessageService hippo4jBaseSendMessageService) {
        return new DynamicThreadPoolConfigService(httpAgent, properties, notifyConfigBuilder, hippo4jBaseSendMessageService);
    }


    //动态线程池处理器，这个处理器其实是就是spring中的一个bean处理器，在这个bean处理器中把动态线程池包装成了DynamicThreadPoolRegisterWrapper对象
    //然后开始服务端注册该动态线程池的信息
    @Bean
    @SuppressWarnings("all")
    public DynamicThreadPoolPostProcessor threadPoolBeanPostProcessor(HttpAgent httpAgent,
                                                                      ApplicationContextHolder hippo4JApplicationContextHolder,
                                                                      DynamicThreadPoolSubscribeConfig dynamicThreadPoolSubscribeConfig) {
        return new DynamicThreadPoolPostProcessor(properties, httpAgent, dynamicThreadPoolSubscribeConfig);
    }


    //远程通信组件，使用的是http通信方式
    @Bean
    public HttpAgent httpAgent(BootstrapProperties properties) {
        return new ServerHttpAgent(properties);
    }

    //这里创建的这个ApplicationContentPostProcessor处理器对象主要是用来发布ContextRefreshedEvent事件的
    //在springboot容器准备完毕之后，这个ContextRefreshedEvent事件就会被ApplicationContentPostProcessor对象发布
    //然后客户端和服务端的心跳检测机制就会开始执行，并且客户端监听服务端配置信息是否发生变化的长轮询操作也会开始执行
    //但第二版本代码我还没有为大家引入长轮询，所以在ApplicationContentPostProcessor这个类中，我把长轮询的部分代码省略了
    @Bean
    public ApplicationContentPostProcessor applicationContentPostProcessor() {
        return new ApplicationContentPostProcessor();
    }

    //用来执行心跳检测的对象
    @Bean
    @SuppressWarnings("all")
    public ServerHealthCheck httpScheduledHealthCheck(HttpAgent httpAgent) {
        return new HttpScheduledHealthCheck(httpAgent);
    }

    //客户端长轮询对象
    @Bean
    public ClientWorker hippo4jClientWorker(HttpAgent httpAgent,
                                            InetUtils hippo4JInetUtils,
                                            ServerHealthCheck serverHealthCheck) {
        //得到服务实例的标识
        //这个其实就是客户端的ip地址+端口号+uuid
        String identify = IdentifyUtil.generate(environment, hippo4JInetUtils);
        return new ClientWorker(httpAgent, identify, serverHealthCheck);
    }

    //动态刷新线程池配置信息的对象,这个对象后面还需要重构
    @Bean
    public ThreadPoolDynamicRefresh threadPoolDynamicRefresh(ThreadPoolConfigChange threadPoolConfigChange) {
        return new ServerThreadPoolDynamicRefresh(threadPoolConfigChange);
    }

    //订阅服务端动态线程池配置信息的对象，这个对象中的方法可以为对应的线程池设置监听器
    //一旦监听到服务器存储的线程信息发生了变化，这里就可以直接动态刷新客户端本地线程池信息
    @Bean
    public DynamicThreadPoolSubscribeConfig dynamicThreadPoolSubscribeConfig(ThreadPoolDynamicRefresh threadPoolDynamicRefresh,
                                                                             ClientWorker clientWorker) {
        return new DynamicThreadPoolSubscribeConfig(threadPoolDynamicRefresh, clientWorker, properties);
    }



    //线程池运行信息发送器，这个对象把消息转换一下，然后发送给服务端，主要是发送收集到的线程的运行信息
    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("all")
    public MessageSender messageSender(HttpAgent httpAgent) {
        return new HttpConnectSender(httpAgent);
    }


    //用来收集动态线程池信息，并且上报给服务器的对象
    @Bean
    public ReportingEventExecutor reportingEventExecutor(BootstrapProperties properties,
                                                         MessageSender messageSender,
                                                         ServerHealthCheck serverHealthCheck) {
        return new ReportingEventExecutor(properties, messageSender, serverHealthCheck);
    }

    //用来收集运行时线程信息的对象
    @Bean
    public RunTimeInfoCollector runTimeInfoCollector() {
        return new RunTimeInfoCollector(properties);
    }

    //用来完善线程池运行信息的对象，主要是收集内存使用情况，线程池描述信息的对象
    @Bean
    @SuppressWarnings("all")
    public ThreadPoolRunStateHandler threadPoolRunStateHandler(InetUtils hippo4JInetUtils) {
        return new ThreadPoolRunStateHandler(hippo4JInetUtils, environment);
    }

    //使用反射得到线程池详细信息的对象,主要是调用方法，堆栈信息等等
    @Bean
    @ConditionalOnMissingBean(value = ThreadDetailState.class)
    public ThreadDetailState baseThreadDetailStateHandler() {
        return new BaseThreadDetailStateHandler();
    }

    //存放dubbo，rocketmq等第三方框架线程池的容器对象
    @Bean
    public ThreadPoolAdapterBeanContainer threadPoolAdapterBeanContainer() {
        return new ThreadPoolAdapterBeanContainer();
    }


    //用来获得线程池对应的通知对象的配置信息
    @Bean
    public NotifyConfigBuilder serverNotifyConfigBuilder(HttpAgent httpAgent,
                                                         BootstrapProperties properties,
                                                         AlarmControlHandler alarmControlHandler) {
        return new ServerNotifyConfigBuilder(httpAgent, properties, alarmControlHandler);
    }


    //检查是否要报警的对象
    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolCheckAlarm defaultThreadPoolCheckAlarmHandler(Hippo4jSendMessageService hippo4jSendMessageService) {
        return new DefaultThreadPoolCheckAlarmHandler(hippo4jSendMessageService);
    }


    //发送报警信息的对象
    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolConfigChange defaultThreadPoolConfigChangeHandler(Hippo4jSendMessageService hippo4jSendMessageService) {
        return new DefaultThreadPoolConfigChangeHandler(hippo4jSendMessageService);
    }

}
