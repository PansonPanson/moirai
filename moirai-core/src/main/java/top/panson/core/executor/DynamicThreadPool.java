package top.panson.core.executor;

import java.lang.annotation.*;



@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicThreadPool {
}