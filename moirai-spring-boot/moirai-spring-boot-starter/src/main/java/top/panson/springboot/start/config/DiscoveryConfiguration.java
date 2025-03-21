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