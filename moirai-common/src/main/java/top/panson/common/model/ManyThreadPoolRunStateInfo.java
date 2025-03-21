package top.panson.common.model;

import lombok.Data;

/**
 * Many pool run state info.
 */
@Data
public class ManyThreadPoolRunStateInfo extends ThreadPoolRunStateInfo {

    /**
     * identify
     */
    private String identify;

    /**
     * active
     */
    private String active;

    /**
     * state
     */
    private String state;
}
