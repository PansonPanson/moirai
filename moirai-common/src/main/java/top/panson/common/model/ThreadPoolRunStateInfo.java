package top.panson.common.model;

import lombok.*;

import java.io.Serializable;

/**
 * Pool run state info.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolRunStateInfo extends ThreadPoolBaseInfo implements Serializable {

    /**
     * currentLoad
     */
    private String currentLoad;

    /**
     * peakLoad
     */
    private String peakLoad;

    /**
     * tpId
     */
    private String tpId;

    /**
     * activeCount
     */
    private Integer activeCount;

    /**
     * poolSize
     */
    private Integer poolSize;

    /**
     * activeSize
     */
    private Integer activeSize;

    /**
     * The maximum number of threads that enter the thread pool at the same time
     */
    private Integer largestPoolSize;

    /**
     * queueSize
     */
    private Integer queueSize;

    /**
     * queueRemainingCapacity
     */
    private Integer queueRemainingCapacity;

    /**
     * completedTaskCount
     */
    private Long completedTaskCount;

    /**
     * rejectCount
     */
    private Long rejectCount;

    /**
     * host
     */
    private String host;

    /**
     * memoryProportion
     */
    private String memoryProportion;

    /**
     * freeMemory
     */
    private String freeMemory;

    /**
     * clientLastRefreshTime
     */
    private String clientLastRefreshTime;

    /**
     * timestamp
     */
    private Long timestamp;

    public Integer getSimpleCurrentLoad() {
        return Integer.parseInt(getCurrentLoad().replace("%", ""));
    }

    public Integer getSimplePeakLoad() {
        return Integer.parseInt(getPeakLoad().replace("%", ""));
    }
}
