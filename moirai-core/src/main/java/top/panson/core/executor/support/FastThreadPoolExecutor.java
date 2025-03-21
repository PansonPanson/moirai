package top.panson.core.executor.support;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/27
 * @方法描述：快速线程池执行器
 */
@Slf4j
public class FastThreadPoolExecutor extends ThreadPoolExecutorTemplate {


    public FastThreadPoolExecutor(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime,
                                  TimeUnit unit,
                                  TaskQueue<Runnable> workQueue,
                                  ThreadFactory threadFactory,
                                  RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    //这个成员变量就是用来记录当前执行器中提交的任务数量
    private final AtomicInteger submittedTaskCount = new AtomicInteger(0);


    //获得像执行器提交的任务数量
    public int getSubmittedTaskCount() {
        return submittedTaskCount.get();
    }

    //这是线程池本身的一个扩展方法，该方法会在线程池执行完任务后执行
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        //因为已经执行完一个任务了，所以把提交任务数量减一
        submittedTaskCount.decrementAndGet();
    }

    //执行任务的方法
    @Override
    public void execute(Runnable command) {
        //提交任务的时候，首先让任务计数自增一
        submittedTaskCount.incrementAndGet();
        try {//调用父类方法执行任务，注意，这里调用父类方法之后，逻辑就会来到TaskQueue类中了
            //这是ThreadPoolExecutor线程池本身的逻辑，会先把任务交给任务队列，这个可以理解吧？所以这时候要看一下TaskQueue队列中的逻辑
            super.execute(command);
        } catch (RejectedExecutionException rx) {
            //添加任务失败，触发了拒绝策略后
            final TaskQueue queue = (TaskQueue) super.getQueue();
            try {//重新尝试添加到队列中
                if (!queue.retryOffer(command, 0, TimeUnit.MILLISECONDS)) {
                    //添加失败则不能执行该任务，提交任务计数减一
                    submittedTaskCount.decrementAndGet();
                    throw new RejectedExecutionException("The blocking queue capacity is full.", rx);
                }
            } catch (InterruptedException x) {
                submittedTaskCount.decrementAndGet();
                throw new RejectedExecutionException(x);
            }
        } catch (Exception t) {
            submittedTaskCount.decrementAndGet();
            throw t;
        }
    }
}
