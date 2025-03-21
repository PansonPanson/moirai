package top.panson.moiraicore.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;



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
