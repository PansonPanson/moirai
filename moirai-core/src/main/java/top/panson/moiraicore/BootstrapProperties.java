package top.panson.moiraicore;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;



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

    /**
     * Thread pool monitoring related configuration.
     */
    //监控信息方面的配置类暂时注释掉
    //private MonitorProperties monitor;

    //下面封装的这几个信息在第一版本代码还用不上，它们都和线程池运行信息的收集有关，等后面引入了线程池信息监控后再讲解
    /***
     * Latest use {@link MonitorProperties#getEnable()}
     */
    @Deprecated
    private Boolean collect = Boolean.TRUE;

    /**
     * Latest use {@link MonitorProperties#getCollectTypes()}
     */
    @Deprecated
    private String collectType;

    /**
     * Latest use {@link MonitorProperties#getInitialDelay()}
     */
    @Deprecated
    private Long initialDelay = 10000L;

    /**
     * Latest use {@link MonitorProperties#getCollectInterval()}
     */
    @Deprecated
    private Long collectInterval = 5000L;

    /**
     * Latest use {@link MonitorProperties#getTaskBufferSize()}
     */
    @Deprecated
    private Integer taskBufferSize = 4096;
}

