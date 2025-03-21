package top.panson.core.executor.state;

import top.panson.common.model.ManyThreadPoolRunStateInfo;
import top.panson.common.model.ThreadPoolRunStateInfo;
import top.panson.common.toolkit.BeanUtil;
import top.panson.common.toolkit.ByteConvertUtil;
import top.panson.common.toolkit.MemoryUtil;
import top.panson.common.toolkit.StringUtil;
import top.panson.core.executor.DynamicThreadPoolWrapper;
import top.panson.core.executor.manage.GlobalThreadPoolManage;
import top.panson.core.toolkit.inet.InetUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.concurrent.ThreadPoolExecutor;

import static top.panson.core.toolkit.IdentifyUtil.CLIENT_IDENTIFICATION_VALUE;


/**
 *
 * @方法描述：线程池运行时状态处理器，在这个处理器中，可以得到线程池运行的额外信息
 */
@Slf4j
@AllArgsConstructor
public class ThreadPoolRunStateHandler extends AbstractThreadPoolRuntime {


    private final InetUtils hippo4JInetUtils;


    private final ConfigurableEnvironment environment;



    @Override
    public ThreadPoolRunStateInfo supplement(ThreadPoolRunStateInfo poolRunStateInfo) {
        //得到jvm堆内存使用大小
        long used = MemoryUtil.heapMemoryUsed();
        //得到可用的最大堆内存
        long max = MemoryUtil.heapMemoryMax();
        //定义一个堆内存描述信息
        String memoryProportion = StringUtil.newBuilder(
                "Allocation: ",
                ByteConvertUtil.getPrintSize(used),
                " / Maximum available: ",
                ByteConvertUtil.getPrintSize(max));
        //设置负载信息
        poolRunStateInfo.setCurrentLoad(poolRunStateInfo.getCurrentLoad() + "%");
        //设置峰值信息，都是添加了%号的值
        poolRunStateInfo.setPeakLoad(poolRunStateInfo.getPeakLoad() + "%");
        //得到客户端的Ip地址
        String ipAddress = hippo4JInetUtils.findFirstNonLoopBackHostInfo().getIpAddress();
        //设置客户端的Ip地址
        poolRunStateInfo.setHost(ipAddress);
        //设置内存描述信息
        poolRunStateInfo.setMemoryProportion(memoryProportion);
        //设置剩余可用内存大小
        poolRunStateInfo.setFreeMemory(ByteConvertUtil.getPrintSize(Math.subtractExact(max, used)));
        //设置线程池Id
        String threadPoolId = poolRunStateInfo.getTpId();
        //从全局线程池管理器中得到线程池包装对象
        DynamicThreadPoolWrapper executorService = GlobalThreadPoolManage.getExecutorService(threadPoolId);
        //得到真正执行人物的线程池
        ThreadPoolExecutor pool = executorService.getExecutor();
        //得到阻塞策略名称
        String rejectedName;
        rejectedName = pool.getRejectedExecutionHandler().getClass().getSimpleName();
        //设置阻塞策略名称
        poolRunStateInfo.setRejectedName(rejectedName);
        //将ThreadPoolRunStateInfo对象转换为ManyThreadPoolRunStateInfo对象
        ManyThreadPoolRunStateInfo manyThreadPoolRunStateInfo = BeanUtil.convert(poolRunStateInfo, ManyThreadPoolRunStateInfo.class);
        //设置客户端唯一标识符
        manyThreadPoolRunStateInfo.setIdentify(CLIENT_IDENTIFICATION_VALUE);
        //设置激活文件信息
        String active = environment.getProperty("spring.profiles.active", "UNKNOWN");
        manyThreadPoolRunStateInfo.setActive(active.toUpperCase());
        //得到并设置线程池状态描述信息
        String threadPoolState = ThreadPoolStatusHandler.getThreadPoolState(pool);
        manyThreadPoolRunStateInfo.setState(threadPoolState);
        return manyThreadPoolRunStateInfo;
    }
}

