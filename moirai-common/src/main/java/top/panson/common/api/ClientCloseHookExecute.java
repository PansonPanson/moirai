package top.panson.common.api;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Client close hook execute.
 */
public interface ClientCloseHookExecute {

    /**
     * Client close hook function execution.
     *
     * @param req
     */
    void closeHook(ClientCloseHookReq req);

    /**
     * Client close hook req.
     */
    @Data
    @Accessors(chain = true)
    class ClientCloseHookReq {

        /**
         * appName
         */
        private String appName;

        /**
         * instanceId
         */
        private String instanceId;

        /**
         * groupKey
         */
        private String groupKey;
    }
}
