package top.panson.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Thread-pool base info.
 */
@Data
@Accessors(chain = true)
public class ThreadPoolBaseInfo {

    /**
     * coreSize
     */
    private Integer coreSize;

    /**
     * maximumSize
     */
    private Integer maximumSize;

    /**
     * queueType
     */
    private String queueType;

    /**
     * queueCapacity
     */
    private Integer queueCapacity;

    /**
     * rejectedName
     */
    private String rejectedName;

    /**
     * keepAliveTime
     */
    private Long keepAliveTime;
}