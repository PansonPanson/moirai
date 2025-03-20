package top.panson.moiraicore;

import lombok.Getter;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;



public enum RejectedPolicyTypeEnum {

    //下面这四种都是jdk自带的拒绝策略，我就不再解释了
    CALLER_RUNS_POLICY(1, "CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy()),


    ABORT_POLICY(2, "AbortPolicy", new ThreadPoolExecutor.AbortPolicy()),


    DISCARD_POLICY(3, "DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy()),


    DISCARD_OLDEST_POLICY(4, "DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy()),

    //这个是一个自定义的拒绝策略，当触发拒绝策略时，程序会立即执行队列中最旧的任务，然后把新任务再次尝试添加到队列中
    //添加失败则直接执行
    RUNS_OLDEST_TASK_POLICY(5, "RunsOldestTaskPolicy", new RunsOldestTaskPolicy()),

    //触发拒绝策略时，会一直同步等待，直到任务添加队列成功
    SYNC_PUT_QUEUE_POLICY(6, "SyncPutQueuePolicy", new SyncPutQueuePolicy());

    @Getter
    private Integer type;

    @Getter
    private String name;

    private RejectedExecutionHandler rejectedHandler;

    RejectedPolicyTypeEnum(Integer type, String name, RejectedExecutionHandler rejectedHandler) {
        this.type = type;
        this.name = name;
        this.rejectedHandler = rejectedHandler;
    }


    static {
        DynamicThreadPoolServiceLoader.register(CustomRejectedExecutionHandler.class);
    }


    public static RejectedExecutionHandler createPolicy(String name) {
        RejectedPolicyTypeEnum rejectedTypeEnum = Stream.of(RejectedPolicyTypeEnum.values())
                .filter(each -> Objects.equals(each.name, name))
                .findFirst()
                .orElse(null);
        if (rejectedTypeEnum != null) {
            return rejectedTypeEnum.rejectedHandler;
        }
        Collection<CustomRejectedExecutionHandler> customRejectedExecutionHandlers = DynamicThreadPoolServiceLoader
                .getSingletonServiceInstances(CustomRejectedExecutionHandler.class);
        Optional<RejectedExecutionHandler> customRejected = customRejectedExecutionHandlers.stream()
                .filter(each -> Objects.equals(name, each.getName()))
                .map(each -> each.generateRejected())
                .findFirst();
        return customRejected.orElse(ABORT_POLICY.rejectedHandler);
    }


    public static RejectedExecutionHandler createPolicy(int type) {
        Optional<RejectedExecutionHandler> rejectedTypeEnum = Stream.of(RejectedPolicyTypeEnum.values())
                .filter(each -> Objects.equals(type, each.type))
                .map(each -> each.rejectedHandler)
                .findFirst();
        RejectedExecutionHandler resultRejected = rejectedTypeEnum.orElseGet(() -> {
            Collection<CustomRejectedExecutionHandler> customRejectedExecutionHandlers = DynamicThreadPoolServiceLoader
                    .getSingletonServiceInstances(CustomRejectedExecutionHandler.class);
            Optional<RejectedExecutionHandler> customRejected = customRejectedExecutionHandlers.stream()
                    .filter(each -> Objects.equals(type, each.getType()))
                    .map(each -> each.generateRejected())
                    .findFirst();
            return customRejected.orElse(ABORT_POLICY.rejectedHandler);
        });
        return resultRejected;
    }


    public static String getRejectedNameByType(int type) {
        return createPolicy(type).getClass().getSimpleName();
    }


    public static RejectedPolicyTypeEnum getRejectedPolicyTypeEnumByName(String name) {
        Optional<RejectedPolicyTypeEnum> rejectedTypeEnum = Stream.of(RejectedPolicyTypeEnum.values())
                .filter(each -> each.name.equals(name))
                .findFirst();
        return rejectedTypeEnum.orElse(ABORT_POLICY);
    }
}
