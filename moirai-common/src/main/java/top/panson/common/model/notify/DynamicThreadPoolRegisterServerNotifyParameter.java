package top.panson.common.model.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dynamic thread-pool register server notify parameter.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicThreadPoolRegisterServerNotifyParameter {

    /**
     * Platform
     */
    private String platform;

    /**
     * Access token
     */
    private String accessToken;

    /**
     * Interval
     */
    private Integer interval;

    /**
     * Receives
     */
    private String receives;
}
