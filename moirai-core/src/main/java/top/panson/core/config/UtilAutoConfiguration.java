package top.panson.core.config;

import top.panson.core.toolkit.inet.InetUtils;
import top.panson.core.toolkit.inet.InetUtilsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Util auto configuration.
 */
@EnableConfigurationProperties(InetUtilsProperties.class)
public class UtilAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InetUtils hippo4JInetUtils(InetUtilsProperties inetUtilsProperties) {
        return new InetUtils(inetUtilsProperties);
    }
}
