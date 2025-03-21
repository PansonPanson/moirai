package top.panson.moiraicore;


import top.panson.moiraicore.adapter.DynamicThreadPoolAdapter;
import top.panson.moiraicore.adapter.ThreadPoolTaskExecutorAdapter;
import top.panson.moiraicore.adapter.TransmittableThreadLocalExecutorAdapter;
import top.panson.moiraicore.adapter.TransmittableThreadLocalExecutorServiceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;



public class DynamicThreadPoolAdapterChoose {

    //存储了所有执行器适配器对象的集合
    private static List<DynamicThreadPoolAdapter> DYNAMIC_THREAD_POOL_ADAPTERS = new ArrayList<>();

    //添加三个动态线程池适配器对象
    static {
        DYNAMIC_THREAD_POOL_ADAPTERS.add(new TransmittableThreadLocalExecutorAdapter());
        DYNAMIC_THREAD_POOL_ADAPTERS.add(new TransmittableThreadLocalExecutorServiceAdapter());
        DYNAMIC_THREAD_POOL_ADAPTERS.add(new ThreadPoolTaskExecutorAdapter());
    }


    //匹配执行器的适配器对象
    public static boolean match(Object executor) {
        return DYNAMIC_THREAD_POOL_ADAPTERS.stream().anyMatch(each -> each.match(executor));
    }


    //使用执行器的适配器对象得到执行器中的动态线程池
    public static DynamicThreadPoolExecutor unwrap(Object executor) {
        Optional<DynamicThreadPoolAdapter> dynamicThreadPoolAdapterOptional = DYNAMIC_THREAD_POOL_ADAPTERS.stream().filter(each -> each.match(executor)).findFirst();
        return dynamicThreadPoolAdapterOptional.map(each -> each.unwrap(executor)).orElse(null);
    }


    //使用dynamicThreadPoolExecutor替代executor中的线程池成员变量
    public static void replace(Object executor, Executor dynamicThreadPoolExecutor) {
        Optional<DynamicThreadPoolAdapter> dynamicThreadPoolAdapterOptional = DYNAMIC_THREAD_POOL_ADAPTERS.stream().filter(each -> each.match(executor)).findFirst();
        if (dynamicThreadPoolAdapterOptional.isPresent()) {
            dynamicThreadPoolAdapterOptional.get().replace(executor, dynamicThreadPoolExecutor);
        }
    }
}