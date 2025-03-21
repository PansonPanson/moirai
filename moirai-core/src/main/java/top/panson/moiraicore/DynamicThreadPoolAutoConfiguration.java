package top.panson.moiraicore;



import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import top.panson.moiraicore.util.net.HttpAgent;
import top.panson.moiraicore.util.net.ServerHttpAgent;



@Configuration
@AllArgsConstructor
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@EnableConfigurationProperties(BootstrapProperties.class)
@ConditionalOnProperty(prefix = BootstrapProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
public class DynamicThreadPoolAutoConfiguration {

    //在这里把配置文件中的相关信息封封装到这个成员变量中了
    private final BootstrapProperties properties;


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


    //创建动态线程池的服务对象，在第一版本代码中提供了注册线程池信息到服务端功能
    @Bean
    @SuppressWarnings("all")
    public DynamicThreadPoolService dynamicThreadPoolConfigService(HttpAgent httpAgent) {
        return new DynamicThreadPoolConfigService(httpAgent, properties);
    }


    //动态线程池处理器，这个处理器其实是就是spring中的一个bean处理器，在这个bean处理器中把动态线程池包装成了DynamicThreadPoolRegisterWrapper对象
    //然后开始向服务端注册该动态线程池的信息
    @Bean
    @SuppressWarnings("all")
    public DynamicThreadPoolPostProcessor threadPoolBeanPostProcessor(HttpAgent httpAgent,
                                                                      ApplicationContextHolder hippo4JApplicationContextHolder) {
        return new DynamicThreadPoolPostProcessor(properties, httpAgent);
    }


    //远程通信组件，使用的是http通信方式
    @Bean
    public HttpAgent httpAgent(BootstrapProperties properties) {
        return new ServerHttpAgent(properties);
    }


}

