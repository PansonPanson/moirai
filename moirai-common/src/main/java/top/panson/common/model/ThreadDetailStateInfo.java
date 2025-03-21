package top.panson.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Thread detail state info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ThreadDetailStateInfo {

    /**
     * threadId
     */
    private Long threadId;

    /**
     * threadName
     */
    private String threadName;

    /**
     * threadStatus
     */
    private String threadStatus;

    /**
     * threadStack
     */
    private List<String> threadStack;
}