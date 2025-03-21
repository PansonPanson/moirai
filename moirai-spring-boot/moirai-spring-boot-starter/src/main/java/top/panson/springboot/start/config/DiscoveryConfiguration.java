package top.panson.springboot.start.config;

import top.panson.common.model.InstanceInfo;
import top.panson.core.toolkit.inet.InetUtils;
import top.panson.springboot.start.core.DiscoveryClient;
import top.panson.springboot.start.provider.InstanceInfoProviderFactory;
import top.panson.springboot.start.remote.HttpAgent;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/6
 * @方法描述：服务发现配置类
 */
@AllArgsConstructor
public class DiscoveryConfiguration {

    private final ConfigurableEnvironment environment;

    private final BootstrapProperties bootstrapProperties;

    private final InetUtils hippo4JInetUtils;

    //创建服务实例对象
    @Bean
    public InstanceInfo instanceConfig() {
        return InstanceInfoProviderFactory.getInstance(environment, bootstrapProperties, hippo4JInetUtils);
    }

    //创建服务发现客户端
    @Bean
    public DiscoveryClient hippo4JDiscoveryClient(HttpAgent httpAgent, InstanceInfo instanceInfo) {
        return new DiscoveryClient(httpAgent, instanceInfo);
    }
}