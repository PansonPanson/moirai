package top.panson.moiraicore;

import lombok.Setter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;




public class TaskQueue<R extends Runnable> extends LinkedBlockingQueue<Runnable> {

    private static final long serialVersionUID = -2635853580887179627L;

    @Setter
    private FastThreadPoolExecutor executor;


    public TaskQueue(int capacity) {
        super(capacity);
    }


    @Override
    public boolean offer(Runnable runnable) {
        //首先获取线程池中当前现成的数量
        int currentPoolThreadSize = executor.getPoolSize();
        //如果已经提交的任务数量小于当前线程数量，那就直接把任务添加到队列中，让线程执行即可
        if (executor.getSubmittedTaskCount() < currentPoolThreadSize) {
            return super.offer(runnable);
        }
        //走到这里意味着提交的任务数量大于当前线程数量了，但是又判断了一下当前线程数量是否小于线程池的最大线程数量
        //如果小于就意味着线程池还可以继续创建线程，那就返回false，让线程池创建线程，再执行任务，这也就是快速线程池快速的原因
        if (currentPoolThreadSize < executor.getMaximumPoolSize()) {
            return false;
        }
        //走到这里意味着线程创建到最大了，就直接往队列中添加即可
        return super.offer(runnable);
    }


    //重新把任务放到队列的方法
    public boolean retryOffer(Runnable runnable, long timeout, TimeUnit unit) throws InterruptedException {
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("Actuator closed!");
        }
        return super.offer(runnable, timeout, unit);
    }
}