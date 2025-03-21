package top.panson.common.monitor;

import java.io.Serializable;
import java.util.List;

/**
 * Abstract message monitoring interface.
 */
public interface Message<T extends Message> extends Serializable {

    /**
     * Get groupKey.
     *
     * @return
     */
    String getGroupKey();

    /**
     * Get message type.
     *
     * @return
     */
    MessageTypeEnum getMessageType();

    /**
     * Get messages.
     *
     * @return
     */
    List<T> getMessages();
}