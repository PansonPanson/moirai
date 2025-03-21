package top.panson.moiraicore;


import top.panson.moiraicore.model.ThreadPoolParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;



public class GlobalThreadPoolManage {


    //存放动态线程池核心参数的map，这个会在动态刷新线程池的时候用到
    private static final Map<String, ThreadPoolParameter> POOL_PARAMETER = new ConcurrentHashMap();

    //存放动态线程池包装对象的map，收集线程运行时信息的时候会用到
    private static final Map<String, DynamicThreadPoolWrapper> EXECUTOR_MAP = new ConcurrentHashMap();


    //根据线程池Id得到对应的DynamicThreadPoolWrapper对象
    public static DynamicThreadPoolWrapper getExecutorService(String threadPoolId) {
        return EXECUTOR_MAP.get(threadPoolId);
    }


    //根据线程池Id得到ThreadPoolExecutor的方法
    public static ThreadPoolExecutor getExecutor(String threadPoolId) {
        return Optional.ofNullable(EXECUTOR_MAP.get(threadPoolId)).map(each -> each.getExecutor()).orElse(null);
    }

    //根据线程池Id得到对用的线程池核心参数对象的方法
    public static ThreadPoolParameter getPoolParameter(String threadPoolId) {
        return POOL_PARAMETER.get(threadPoolId);
    }


    //把线程池核心参数信息和线程池包装对象交给线程池全局管理器管理的方法
    public static void register(String threadPoolId, ThreadPoolParameter threadPoolParameter, DynamicThreadPoolWrapper executor) {
        registerPool(threadPoolId, executor);
        registerPoolParameter(threadPoolId, threadPoolParameter);
    }


    public static void registerPool(String threadPoolId, DynamicThreadPoolWrapper executor) {
        EXECUTOR_MAP.put(threadPoolId, executor);
    }


    public static void registerPoolParameter(String threadPoolId, ThreadPoolParameter threadPoolParameter) {
        POOL_PARAMETER.put(threadPoolId, threadPoolParameter);
    }



    public static void dynamicRegister(DynamicThreadPoolRegisterWrapper registerWrapper) {
        //这里从ApplicationContext中得到了DynamicThreadPoolService对象
        DynamicThreadPoolService dynamicThreadPoolService = ApplicationContextHolder.getBean(DynamicThreadPoolService.class);
        //DynamicThreadPoolService对象把动态线程池信息注册到服务端
        dynamicThreadPoolService.registerDynamicThreadPool(registerWrapper);
    }


    public static List<String> listThreadPoolId() {
        return new ArrayList<>(EXECUTOR_MAP.keySet());
    }


    public static Integer getThreadPoolNum() {
        return listThreadPoolId().size();
    }
}
