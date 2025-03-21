package top.panson.moiraicore;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import top.panson.moiraicore.plugin.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;



public class ExtensibleThreadPoolExecutor extends ThreadPoolExecutor implements ThreadPoolPluginSupport {


    //线程池Id
    @Getter
    private final String threadPoolId;

    //插件管理器对象，当前线程池用到的所有插件都会注册到这个管理器中
    @Getter
    private final ThreadPoolPluginManager threadPoolPluginManager;


    //决策策略处理器的包装对象，其实就是把拒绝策略的插件对象的方法加上了
    private final RejectedAwareHandlerWrapper handlerWrapper;


    public ExtensibleThreadPoolExecutor(
            @NonNull String threadPoolId,
            @NonNull ThreadPoolPluginManager threadPoolPluginManager,
            int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit,
            @NonNull BlockingQueue<Runnable> workQueue,
            @NonNull ThreadFactory threadFactory,
            @NonNull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        // Pool extended info.
        this.threadPoolId = threadPoolId;
        this.threadPoolPluginManager = threadPoolPluginManager;
        // Proxy handler to support Aware callback.
        while (handler instanceof RejectedAwareHandlerWrapper) {
            handler = ((RejectedAwareHandlerWrapper) handler).getHandler();
        }
        this.handlerWrapper = new RejectedAwareHandlerWrapper(threadPoolPluginManager, handler);
        super.setRejectedExecutionHandler(handlerWrapper);
    }


    //该方法会在线程执行任务之前被调用
    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        //这里从插件处理器中得到了ExecuteAwarePlugin类型的所有插件，这个插件的对象是用来计算任务耗时和任务是否超时了
        Collection<ExecuteAwarePlugin> executeAwarePluginList = threadPoolPluginManager.getExecuteAwarePluginList();
        //执行了插件对象中的beforeExecute方法
        executeAwarePluginList.forEach(aware -> aware.beforeExecute(thread, runnable));
    }


    //线程执行任务的方法
    @Override
    public void execute(@NonNull Runnable runnable) {
        //在这里得到了TaskDecoratorPlugin装饰器插件对象，这个装饰器插件对象中存放了装饰器对象
        //装饰器对象可以对要执行的任务做一层额外的包装，可以做一些扩展逻辑
        Collection<TaskAwarePlugin> taskAwarePluginList = threadPoolPluginManager.getTaskAwarePluginList();
        for (TaskAwarePlugin taskAwarePlugin : taskAwarePluginList) {
            //执行了装饰器对象的beforeTaskExecute方法，该方法会返回一个新的runnable，这个runnable不仅包含了原生任务的逻辑
            //还有装饰器对象新添加的逻辑
            runnable = taskAwarePlugin.beforeTaskExecute(runnable);
        }//然后再开始执行任务
        super.execute(runnable);
    }


    //该方法会在任务执行完毕之后被调用
    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        //这里从插件处理器中再次得到了ExecuteAwarePlugin类型的所有插件
        Collection<ExecuteAwarePlugin> executeAwarePluginList = threadPoolPluginManager.getExecuteAwarePluginList();
        //执行插件对象中的afterExecute方法，计算任务耗时和任务是否超时
        executeAwarePluginList.forEach(aware -> aware.afterExecute(runnable, throwable));
    }


    //该方法会在线程池被关闭的时候调用
    @Override
    public void shutdown() {
        //得到ShutdownAwarePlugin类型的插件
        Collection<ShutdownAwarePlugin> shutdownAwarePluginList = threadPoolPluginManager.getShutdownAwarePluginList();
        //执行插件中的扩展方法
        shutdownAwarePluginList.forEach(aware -> aware.beforeShutdown(this));
        super.shutdown();
        shutdownAwarePluginList.forEach(aware -> aware.afterShutdown(this, Collections.emptyList()));
    }


    //同上，只不过这个方法是立即关闭线程池
    @Override
    public List<Runnable> shutdownNow() {
        Collection<ShutdownAwarePlugin> shutdownAwarePluginList = threadPoolPluginManager.getShutdownAwarePluginList();
        //执行插件中的方法
        shutdownAwarePluginList.forEach(aware -> aware.beforeShutdown(this));
        //这里执行的就是立即停止线程池工作的方法，该方法会把还未执行的任务封装到list中返回给用户
        //这里是看看剩下的任务是不是FutureTask，如果是那么就可以在插件中把这些任务都取消了
        List<Runnable> tasks = super.shutdownNow();
        shutdownAwarePluginList.forEach(aware -> aware.afterShutdown(this, tasks));
        return tasks;
    }


    //这个也是线程池中的一个拓展方法，在ThreadPoolExecutor中并没有实现，用户可以自己实现这个方法
    //该方法会在线程池转变为TERMINATED状态时被调用
    @Override
    protected void terminated() {
        super.terminated();
        Collection<ShutdownAwarePlugin> shutdownAwarePluginList = threadPoolPluginManager.getShutdownAwarePluginList();
        shutdownAwarePluginList.forEach(aware -> aware.afterTerminated(this));
    }


    //这里就是AbstractExecutorService执行器中的方法了，创建FutureTask任务交给线程池执行
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        //照样是得到任务的装饰器对象插件，然后先执行装饰器对象的逻辑，对任务做包装
        Collection<TaskAwarePlugin> taskAwarePluginList = threadPoolPluginManager.getTaskAwarePluginList();
        for (TaskAwarePlugin taskAwarePlugin : taskAwarePluginList) {
            runnable = taskAwarePlugin.beforeTaskCreate(this, runnable, value);
        }
        return super.newTaskFor(runnable, value);
    }


    //同上，只不过这里执行的是有返回值的任务
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        Collection<TaskAwarePlugin> taskAwarePluginList = threadPoolPluginManager.getTaskAwarePluginList();
        for (TaskAwarePlugin taskAwarePlugin : taskAwarePluginList) {
            callable = taskAwarePlugin.beforeTaskCreate(this, callable);
        }
        return super.newTaskFor(callable);
    }


    //设置拒绝策略处理器到拒绝策略包装器中
    @Override
    public void setRejectedExecutionHandler(@NonNull RejectedExecutionHandler handler) {
        while (handler instanceof RejectedAwareHandlerWrapper) {
            handler = ((RejectedAwareHandlerWrapper) handler).getHandler();
        }
        handlerWrapper.setHandler(handler);
    }


    //得到拒绝策略处理器
    @Override
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return handlerWrapper.getHandler();
    }


    //得到贤臣吃
    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return this;
    }




    @AllArgsConstructor
    private static class RejectedAwareHandlerWrapper implements RejectedExecutionHandler {

        //插件管理器
        private final ThreadPoolPluginManager registry;

        //真正的拒绝策略处理器
        @Setter
        @Getter
        private RejectedExecutionHandler handler;


        //在执行拒绝策略之前，会先执行拒绝策略插件对象中的方法，就是执行通知告警功能
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Collection<RejectedAwarePlugin> rejectedAwarePluginList = registry.getRejectedAwarePluginList();
            rejectedAwarePluginList.forEach(aware -> aware.beforeRejectedExecution(r, executor));
            handler.rejectedExecution(r, executor);
        }
    }
}
