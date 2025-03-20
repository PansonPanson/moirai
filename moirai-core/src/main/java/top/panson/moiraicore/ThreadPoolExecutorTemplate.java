package top.panson.moiraicore;


import top.panson.moiraicore.util.ArrayUtil;

import java.util.concurrent.*;



public class ThreadPoolExecutorTemplate extends ThreadPoolExecutor {

    public ThreadPoolExecutorTemplate(int corePoolSize,
                                      int maximumPoolSize,
                                      long keepAliveTime,
                                      TimeUnit unit,
                                      BlockingQueue<Runnable> workQueue,
                                      ThreadFactory threadFactory,
                                      RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    //执行快速线程池任务的方法
    @Override
    public void execute(final Runnable command) {
        //在执行之前调用了clientTrace()方法，获得了一个异常对象
        //然后调用了wrap方法
        super.execute(wrap(command, clientTrace()));
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return super.submit(wrap(task, clientTrace()));
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return super.submit(wrap(task, clientTrace()));
    }


    //获得异常对象的方法
    private Exception clientTrace() {
        return new Exception("Tread task root stack trace.");
    }


    //包装任务的方法，在这个方法中传进了一个异常对象，这个异常对象的作用就是在任务执行出现异常时，快速定位任务提交的源头
    private Runnable wrap(final Runnable task, final Exception clientStack) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                e.setStackTrace(ArrayUtil.addAll(clientStack.getStackTrace(), e.getStackTrace()));
                throw e;
            }
        };
    }


    private <T> Callable<T> wrap(final Callable<T> task, final Exception clientStack) {
        return () -> {
            try {
                return task.call();
            } catch (Exception e) {
                e.setStackTrace(ArrayUtil.addAll(clientStack.getStackTrace(), e.getStackTrace()));
                throw e;
            }
        };
    }
}
