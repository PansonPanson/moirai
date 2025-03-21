package top.panson.springboot.start.config;

import top.panson.monitor.base.MonitorThreadPoolTypeEnum;
import top.panson.monitor.base.MonitorTypeEnum;
import lombok.Data;

/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：线程池收集信息的配置类
 */
@Data
public class MonitorProperties {


    //是否开启收集线程池运行信息的功能，默认开启
    private Boolean enable = Boolean.TRUE;

    //收集线程池运行信息的类型，这里使用server就代表为服务器收集线程池运行信息
    //也可以选择为ELASTICSEARCH收集线程池信息
    private String collectTypes = MonitorTypeEnum.SERVER.toString().toLowerCase();

    //收集的线程池类型，这里代表收集的是动态线程池的运行信息，也可以设置成web线程池，dubbo线程池
    private String threadPoolTypes = MonitorThreadPoolTypeEnum.DYNAMIC.toString().toLowerCase();

    //执行收集信息的延时时间，也就是10s之后再收集
    //也就是程序启动10秒之后再收集线程池信息
    private Long initialDelay = 10000L;

    //收集信息的间隔时间，每隔5秒执行一次收集信息任务
    private Long collectInterval = 5000L;

    //存放收集到的信息的队列的容量
    private Integer taskBufferSize = 4096;
}