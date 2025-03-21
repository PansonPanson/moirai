package top.panson.springboot.start.config;

import top.panson.core.config.BootstrapPropertiesInterface;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/26
 * @方法描述：这个类封装的就是配置文件中的一些信息
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = BootstrapProperties.PREFIX)
public class BootstrapProperties implements BootstrapPropertiesInterface {

    public static final String PREFIX = "spring.dynamic.thread-pool";

    //用户名
    private String username;

    //密码
    private String password;

    //服务端地址
    private String serverAddr;

    //netty服务器的端口号，这个是可配置的
    //在hippo4j框架，提供了两种通信方式，一种是http，一种就是netty
    //在该框架中默认使用的是http，所以我就不引入netty了
    private String nettyServerPort;

    //客户端上报给服务端线程池历史信息的方法，这个也可以使用netty的方式上报
    //我仍然使用内部默认的http了，不引入netty
    private String reportType;

    //命名空间
    private String namespace;

    //项目Id
    private String itemId;

    //是否启动动态线程池
    private Boolean enable = true;

    //是否在控制台打印hippo4j的启动图案
    private Boolean banner = true;

    //监控配置信息对象
    private MonitorProperties monitor;


    //下面几个成员变量放到了MonitorProperties类中
    @Deprecated
    private Boolean collect = Boolean.TRUE;


    @Deprecated
    private String collectType;

    @Deprecated
    private Long initialDelay = 10000L;


    @Deprecated
    private Long collectInterval = 5000L;

    @Deprecated
    private Integer taskBufferSize = 4096;
}

