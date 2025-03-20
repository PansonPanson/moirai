package top.panson.moiraicore;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Adapted to an earlier version of SpringBoot.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicThreadPoolAnnotationUtil {

    /**
     * Check for the existence of {@param annotationType} based on {@param beanName}.
     *
     * @param beanName       bean name
     * @param annotationType annotation class
     * @param <A>            the Annotation type
     */
    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
        AbstractApplicationContext context = (AbstractApplicationContext) ApplicationContextHolder.getInstance();
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        return Optional.of(beanFactory)
                .map(each -> (RootBeanDefinition) beanFactory.getMergedBeanDefinition(beanName))
                .map(RootBeanDefinition::getResolvedFactoryMethod)
                .map(factoryMethod -> AnnotationUtils.getAnnotation(factoryMethod, annotationType))
                .orElse(null);
    }
}