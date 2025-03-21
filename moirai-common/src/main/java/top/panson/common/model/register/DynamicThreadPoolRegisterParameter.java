package top.panson.common.model.register;

import top.panson.common.executor.support.BlockingQueueTypeEnum;
import top.panson.common.executor.support.RejectedPolicyTypeEnum;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * @方法描述：封装动态线程池核心信息的对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicThreadPoolRegisterParameter {

    /**
     * Thread-pool id
     * Empty or empty strings are not allowed, and `+` signs are not allowed
     */
    private String threadPoolId;

    /**
     * Content
     */
    private String content;

    /**
     * Core pool size
     */
    private Integer corePoolSize;

    /**
     * Maximum pool size
     */
    private Integer maximumPoolSize;

    /**
     * Blocking queue type
     */
    private BlockingQueueTypeEnum blockingQueueType;

    /**
     * Capacity
     */
    private Integer capacity;

    /**
     * Keep alive time
     */
    private Long keepAliveTime;

    /**
     * Rejected policy type
     */
    private RejectedPolicyTypeEnum rejectedPolicyType;

    /**
     * Is alarm
     */
    private Boolean isAlarm;

    /**
     * Capacity alarm
     */
    private Integer capacityAlarm;

    /**
     * Active alarm
     */
    @JsonAlias("livenessAlarm")
    private Integer activeAlarm;

    /**
     * Allow core thread timeout
     */
    private Boolean allowCoreThreadTimeOut;

    /**
     * Thread name prefix
     */
    private String threadNamePrefix;

    /**
     * Execute timeout
     */
    private Long executeTimeOut;

    public Integer getIsAlarm() {
        return this.isAlarm ? 1 : 0;
    }

    public Integer getAllowCoreThreadTimeOut() {
        return this.allowCoreThreadTimeOut ? 1 : 0;
    }
}
