package top.panson.core.executor.state;

import top.panson.common.toolkit.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;



/**
 *
 * @方法描述：线程池状态处理器
 */
@Slf4j
public class ThreadPoolStatusHandler {

    //正在运行状态
    private static final String RUNNING = "Running";

    //终止状态
    private static final String TERMINATED = "Terminated";

    //正在关闭状态
    private static final String SHUTTING_DOWN = "Shutting down";

    //异常标记
    private static final AtomicBoolean EXCEPTION_FLAG = new AtomicBoolean(Boolean.TRUE);


    /**
     * @方法描述：获得线程池状态的方法
     */
    public static String getThreadPoolState(ThreadPoolExecutor executor) {
        //判断是否有异常，没有异常返回true
        if (EXCEPTION_FLAG.get()) {
            try {
                //使用反射得到线程池的runStateLessThan方法，其实就是这个方法
                // private static boolean runStateLessThan(int c, int s) {
                //        return c < s;
                // }
                //该方法就是用来得到线程当前状态的，具体逻辑大家可以去线程池查看，就是通过ctl.get()得到的值和线程池内预制的几种状态作比较
                //判断出线程的目前状态
                Method runStateLessThan = ReflectUtil.getMethodByName(ThreadPoolExecutor.class, "runStateLessThan");
                //设置方法可访问
                ReflectUtil.setAccessible(runStateLessThan);
                //得到线程池的ctl成员变量
                AtomicInteger ctl = (AtomicInteger) ReflectUtil.getFieldValue(executor, "ctl");
                //得到线程池的SHUTDOWN的值
                int shutdown = (int) ReflectUtil.getFieldValue(executor, "SHUTDOWN");
                //反射执行runStateLessThan方法，判断ctl.get()是否<SHUTDOWN
                boolean runStateLessThanBool = ReflectUtil.invoke(executor, runStateLessThan, ctl.get(), shutdown);
                //如果小于意味着线程池处于正在运行状态
                if (runStateLessThanBool) {
                    return RUNNING;
                }
                //注意，当程序执行到这里，就意味着线程池一定不是RUNNING状态了，如果还是RUNNING状态，肯定就在上面退出方法了
                //走到这里意味着线程肯定要关闭了，只是不确定是SHUTTING_DOWN或者TERMINATED状态，所以要继续判断
                //使用反射得到线程池的runStateAtLeast方法，其实就是这个方法
                //private static boolean runStateAtLeast(int c, int s) {
                //        return c >= s;
                //}
                //其实就是用来判断线程池当前的状态是否超过了某个状态
                Method runStateAtLeast = ReflectUtil.getMethodByName(ThreadPoolExecutor.class, "runStateAtLeast");
                //设置可访问
                ReflectUtil.setAccessible(runStateAtLeast);
                //得到线程池的TERMINATED的值
                int terminated = (int) ReflectUtil.getFieldValue(executor, "TERMINATED");
                //反射执行runStateLessThan方法，判断ctl.get()是否>TERMINATED
                //得到线程池最终状态
                String resultStatus = ReflectUtil.invoke(executor, runStateAtLeast, ctl.get(), terminated) ? TERMINATED : SHUTTING_DOWN;
                return resultStatus;
            } catch (Exception ex) {
                log.error("Failed to get thread pool status.", ex);
                //有异常则设置EXCEPTION_FLAG为false
                EXCEPTION_FLAG.set(Boolean.FALSE);
            }
        }
        return "UNKNOWN";
    }
}
