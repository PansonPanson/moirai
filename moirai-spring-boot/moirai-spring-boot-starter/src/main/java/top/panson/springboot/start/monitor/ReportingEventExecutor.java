package top.panson.springboot.start.monitor;

import top.panson.common.config.ApplicationContextHolder;
import top.panson.common.design.builder.ThreadFactoryBuilder;
import top.panson.common.monitor.Message;
import top.panson.common.toolkit.CollectionUtil;
import top.panson.common.toolkit.StringUtil;
import top.panson.common.toolkit.ThreadUtil;
import top.panson.core.executor.manage.GlobalThreadPoolManage;
import top.panson.monitor.base.MonitorTypeEnum;
import top.panson.springboot.start.config.BootstrapProperties;
import top.panson.springboot.start.config.MonitorProperties;
import top.panson.springboot.start.monitor.collect.Collector;
import top.panson.springboot.start.monitor.send.MessageSender;
import top.panson.springboot.start.remote.ServerHealthCheck;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static top.panson.core.executor.manage.GlobalThreadPoolManage.getThreadPoolNum;


/**
 * @方法描述：动态线程池运行信息收集器
 */
@Slf4j
@RequiredArgsConstructor
public class ReportingEventExecutor implements Runnable, CommandLineRunner, DisposableBean {

    @NonNull
    private final BootstrapProperties properties;

    //线程池运行信息发送器，这个对象会把动态线程池的运行信息发送给服务端
    @NonNull
    private final MessageSender messageSender;

    //服务端健康检查器
    @NonNull
    private final ServerHealthCheck serverHealthCheck;

    //存放线程池信息收集器的map
    private Map<String, Collector> collectors;

    //存放线程池实时监控器的集合，这个ThreadPoolMonitor功能我给省略了
    //因为要使用这个功能还要引入
    // <dependency>
    //    <groupId>io.micrometer</groupId>
    //    <artifactId>micrometer-registry-prometheus</artifactId>
    //</dependency>
    //
    //<dependency>
    //    <groupId>org.springframework.boot</groupId>
    //    <artifactId>spring-boot-starter-actuator</artifactId>
    //</dependency>，我不想搞这么麻烦了
    //并且我可以很负责任的告诉大家，这个ThreadPoolMonitor最终收集的信息实际上使用的也是Collector对象
    //而Collector我在第五版本为大家实现得非常全面，无非是ThreadPoolMonitor得到了信息之后把这些信息交给Metrics
    //private List<ThreadPoolMonitor> threadPoolMonitors;

    //线程池信息收集器收集到的线程池信息，都会封装为Message，然后存放到这个队列中，ReportingEventExecutor会从队列中取出这些信息不断上报给服务端
    private BlockingQueue<Message> messageCollectVessel;

    //信息收集执行器，其实就是一个定时任务执行器
    private ScheduledThreadPoolExecutor collectVesselExecutor;


    /**
     * @方法描述：这个就是ReportingEventExecutor这个任务要执行得操作
     */
    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            try {//从存放线程池信息的队列中不断去除线程池信息，阻塞获取
                Message message = messageCollectVessel.take();
                //发送给服务端
                messageSender.send(message);
            } catch (Throwable ex) {
                log.error("Consumption buffer container task failed. Number of buffer container tasks: {}", messageCollectVessel.size(), ex);
            }
        }
    }


    /**
     * @方法描述：这个是springboot一个扩展接口中的方法，就是CommandLineRunner接口中的方法
     */
    @Override
    public void run(String... args) {
        //得到监控配置信息
        MonitorProperties monitor = properties.getMonitor();
        //如果配置信息为空，或者没有开启监控功能，直接退出方法
        if (monitor == null
                || !monitor.getEnable()
                || StringUtil.isBlank(monitor.getThreadPoolTypes())
                || StringUtil.isBlank(monitor.getCollectTypes())) {
            return;
        }
        //得到配置信息中定义的收集线程池运行信息的收集器类型
        String collectType = Optional.ofNullable(StringUtil.emptyToNull(monitor.getCollectTypes())).orElse(MonitorTypeEnum.SERVER.name().toLowerCase());
        //创建定时任务执行器
        collectVesselExecutor = new ScheduledThreadPoolExecutor(new Integer(collectType.split(",").length), ThreadFactoryBuilder.builder().daemon(true).prefix("client.scheduled.collect.data").build());
        //判断线程池信息收集器的类型是不是SERVER，在配置文件中定义的收集器类型就是SERVER，意味着为服务端收集线程池运行信息
        if (collectType.contains(MonitorTypeEnum.SERVER.name().toLowerCase())) {
            //向定时任务执行器中提交定时任务
            collectVesselExecutor.scheduleWithFixedDelay(
                    //定期执行runTimeGatherTask方法，也就是定期收集线程池运行信息
                    () -> runTimeGatherTask(),
                    //设置延迟执行时间
                    properties.getInitialDelay(),
                    //设置定时任务执行周期，也就是收集线程池信息的间隔
                    properties.getCollectInterval(),
                    TimeUnit.MILLISECONDS);
            //得到messageCollectVessel队列的大小
            Integer bufferSize = properties.getTaskBufferSize();
            //创建存放线程池信息的队列
            messageCollectVessel = new ArrayBlockingQueue(bufferSize);
            //从springboot容器中得到收集器
            collectors = ApplicationContextHolder.getBeansOfType(Collector.class);
            //在这里创建了一个新的线程，线程执行的任务就是ReportingEventExecutor本身
            ThreadUtil.newThread(this, "client.thread.reporting.task", Boolean.TRUE).start();
        }
        if (GlobalThreadPoolManage.getThreadPoolNum() > 0) {
            log.info("Dynamic thread pool: [{}]. The dynamic thread pool starts data collection and reporting.", getThreadPoolNum());
        }
    }

    @Override
    public void destroy() {
        Optional.ofNullable(collectVesselExecutor).ifPresent((each) -> each.shutdown());
    }



    /**
     * @方法描述：收集动态线程池运行信息的方法
     */
    private void runTimeGatherTask() {
        //检查服务器健康状态
        boolean healthStatus = serverHealthCheck.isHealthStatus();
        //服务器不健康则直接退出方法
        if (!healthStatus || CollectionUtil.isEmpty(collectors)) {
            return;
        }
        //遍历收集器
        collectors.forEach((beanName, collector) -> {
            //收集器收集线程池运行信息
            Message message = collector.collectMessage();
            //把收集到的信息添加到信息队列中
            boolean offer = messageCollectVessel.offer(message);
            if (!offer) {
                log.warn("Buffer data starts stacking data...");
            }
        });
    }
}
