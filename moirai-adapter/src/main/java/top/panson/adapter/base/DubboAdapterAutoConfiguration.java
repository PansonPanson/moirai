package top.panson.adapter.base;

import top.panson.common.config.ApplicationContextHolder;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo adapter auto configuration.
 */
@Configuration
public class DubboAdapterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApplicationContextHolder simpleApplicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @SuppressWarnings("all")
    @ConditionalOnProperty(name = "dubbo.application.name")
    public DubboThreadPoolAdapter dubboThreadPoolAdapter(ApplicationContextHolder applicationContextHolder) {
        return new DubboThreadPoolAdapter();
    }
}