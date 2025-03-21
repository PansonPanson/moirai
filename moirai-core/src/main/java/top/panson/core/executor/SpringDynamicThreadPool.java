package top.panson.core.executor;

import org.springframework.context.annotation.Bean;

import java.lang.annotation.*;

/**
 * A convenience annotation that is itself annotated with
 * {@link Bean @Bean} and {@link DynamicThreadPool @DynamicThreadPool}.
 *
 * @since 1.4.2
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Bean
@DynamicThreadPool
public @interface SpringDynamicThreadPool {
}
