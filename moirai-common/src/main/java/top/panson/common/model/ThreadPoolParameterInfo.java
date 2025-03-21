package top.panson.common.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/26
 * @方法描述：封装动态线程池核心信息的对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ThreadPoolParameterInfo implements ThreadPoolParameter, Serializable {

    private static final long serialVersionUID = -7123935122108553864L;

    /**
     * Tenant id
     */
    private String tenantId;

    /**
     * Item id
     */
    private String itemId;

    /**
     * Thread-pool id
     */
    @JsonAlias("threadPoolId")
    private String tpId;

    /**
     * Content
     */
    private String content;

    /**
     * Core size
     */
    @Deprecated
    private Integer coreSize;

    /**
     * Max size
     */
    @Deprecated
    private Integer maxSize;

    /**
     * Core pool size
     */
    private Integer corePoolSize;

    /**
     * Maximum pool size
     */
    private Integer maximumPoolSize;

    /**
     * Queue type
     */
    private Integer queueType;

    /**
     * Capacity
     */
    private Integer capacity;

    /**
     * Keep alive time
     */
    private Integer keepAliveTime;

    /**
     * Execute time out
     */
    private Long executeTimeOut;

    /**
     * Rejected type
     */
    private Integer rejectedType;

    /**
     * Is alarm
     */
    private Integer isAlarm;

    /**
     * Capacity alarm
     */
    private Integer capacityAlarm;

    /**
     * Liveness alarm
     */
    @JsonAlias("activeAlarm")
    private Integer livenessAlarm;

    /**
     * Allow core thread timeout
     */
    private Integer allowCoreThreadTimeOut;

    public Integer corePoolSizeAdapt() {
        return this.corePoolSize == null ? this.coreSize : this.corePoolSize;
    }

    public Integer maximumPoolSizeAdapt() {
        return this.maximumPoolSize == null ? this.maxSize : this.maximumPoolSize;
    }
}
