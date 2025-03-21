package top.panson.springboot.start.core;

import top.panson.common.api.ThreadDetailState;
import top.panson.common.model.ThreadDetailStateInfo;
import top.panson.common.toolkit.CollectionUtil;
import top.panson.common.toolkit.ReflectUtil;
import top.panson.core.executor.DynamicThreadPoolWrapper;
import top.panson.core.executor.manage.GlobalThreadPoolManage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @方法描述：线程池中的线程详细状态处理器，使用这个处理器可以得到线程的方法栈
 */
@Slf4j
public class BaseThreadDetailStateHandler implements ThreadDetailState {

    //在ThreadPoolExecutor中，workers成员变量存放了所有线程，这个知识大家应该都知道吧
    //private final HashSet<Worker> workers = new HashSet<Worker>();
    //private final class Worker extends AbstractQueuedSynchronizer implements Runnable{
    //        final Thread thread;
    //        Worker(Runnable firstTask) {
    //            setState(-1); // inhibit interrupts until runWorker
    //            this.firstTask = firstTask;
    //            this.thread = getThreadFactory().newThread(this);
    //        }这是线程池中的代码
    private final String WORKERS = "workers";

    //用来反射获得Worker中的thread成员变量
    private final String THREAD = "thread";


    //根据线程池Id获得线程池中线程的方法栈
    @Override
    public List<ThreadDetailStateInfo> getThreadDetailStateInfo(String threadPoolId) {
        DynamicThreadPoolWrapper dynamicThreadPoolWrapper = GlobalThreadPoolManage.getExecutorService(threadPoolId);
        ThreadPoolExecutor threadPoolExecutor = dynamicThreadPoolWrapper.getExecutor();
        //得到线程的方法栈
        return getThreadDetailStateInfo(threadPoolExecutor);
    }



    @Override
    public List<ThreadDetailStateInfo> getThreadDetailStateInfo(ThreadPoolExecutor threadPoolExecutor) {
        List<ThreadDetailStateInfo> resultThreadStates = new ArrayList();
        try {
            //反射得到线程池中的workers集合
            HashSet<Object> workers = (HashSet<Object>) ReflectUtil.getFieldValue(threadPoolExecutor, WORKERS);
            //对workers判空
            if (CollectionUtil.isEmpty(workers)) {
                return resultThreadStates;
            }
            //开始遍历workers集合
            for (Object worker : workers) {
                Thread thread;
                try {
                    //从遍历到的Worker对象中获得thread成员变量，这时候就把真正执行任务的线程得到了
                    thread = (Thread) ReflectUtil.getFieldValue(worker, THREAD);
                    //对线程进行判空操作
                    if (thread == null) {
                        log.warn("Reflection get worker thread is null. Worker: {}", worker);
                        continue;
                    }
                } catch (Exception ex) {
                    log.error("Reflection get worker thread exception. Worker: {}", worker, ex);
                    continue;
                }
                //得到线程Id
                long threadId = thread.getId();
                //得到线程名称
                String threadName = thread.getName();
                //得到线程状态
                String threadStatus = thread.getState().name();
                //得到线程栈
                StackTraceElement[] stackTrace = thread.getStackTrace();
                List<String> threadStack = new ArrayList(stackTrace.length);
                //存放栈中的方法信息
                for (int i = 0; i < stackTrace.length; i++) {
                    threadStack.add(stackTrace[i].toString());
                }
                //创建ThreadDetailStateInfo对象封装线程信息
                ThreadDetailStateInfo threadState = ThreadDetailStateInfo.builder()
                        .threadId(threadId)
                        .threadName(threadName)
                        .threadStatus(threadStatus)
                        .threadStack(threadStack)
                        .build();
                resultThreadStates.add(threadState);
            }
        } catch (Exception ex) {
            log.error("Failed to get thread status.", ex);
        }
        return resultThreadStates;
    }
}
