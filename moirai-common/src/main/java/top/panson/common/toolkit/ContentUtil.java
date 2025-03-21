package top.panson.common.toolkit;

import top.panson.common.constant.Constants;
import top.panson.common.model.ThreadPoolParameter;
import top.panson.common.model.ThreadPoolParameterInfo;

/**
 * Content util.
 */
public class ContentUtil {

    /**
     * Get pool content.
     *
     * @param parameter thread-pool parameter
     * @return dynamic thread-pool content str
     */
    public static String getPoolContent(ThreadPoolParameter parameter) {
        ThreadPoolParameterInfo threadPoolParameterInfo = new ThreadPoolParameterInfo();
        threadPoolParameterInfo.setTenantId(parameter.getTenantId())
                .setItemId(parameter.getItemId())
                .setTpId(parameter.getTpId())
                .setCoreSize(parameter.getCoreSize())
                .setMaxSize(parameter.getMaxSize())
                .setQueueType(parameter.getQueueType())
                .setCapacity(parameter.getCapacity())
                .setKeepAliveTime(parameter.getKeepAliveTime())
                .setExecuteTimeOut(parameter.getExecuteTimeOut())
                .setIsAlarm(parameter.getIsAlarm())
                .setCapacityAlarm(parameter.getCapacityAlarm())
                .setLivenessAlarm(parameter.getLivenessAlarm())
                .setAllowCoreThreadTimeOut(parameter.getAllowCoreThreadTimeOut())
                .setRejectedType(parameter.getRejectedType());
        return JSONUtil.toJSONString(threadPoolParameterInfo);
    }

    /**
     * Get group key.
     *
     * @param parameter thread-pool parameter
     * @return group key
     */
    public static String getGroupKey(ThreadPoolParameter parameter) {
        return StringUtil.createBuilder()
                .append(parameter.getTpId())
                .append(Constants.GROUP_KEY_DELIMITER)
                .append(parameter.getItemId())
                .append(Constants.GROUP_KEY_DELIMITER)
                .append(parameter.getTenantId())
                .toString();
    }

    /**
     * Get group key.
     *
     * @param parameters thread-pool parameters
     * @return group key
     */
    public static String getGroupKey(String... parameters) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            stringBuilder.append(parameters[i]);
            if (i < parameters.length - 1) {
                stringBuilder.append(Constants.GROUP_KEY_DELIMITER);
            }
        }
        return stringBuilder.toString();
    }
}
