package top.panson.config.service.biz;

import top.panson.common.model.register.DynamicThreadPoolRegisterWrapper;
import top.panson.config.model.ConfigAllInfo;

/**
 * Config service.
 */
public interface ConfigService {

    /**
     * Find config all info.
     *
     * @param tpId     tpId
     * @param itemId   itemId
     * @param tenantId tenantId
     * @return all config
     */
    ConfigAllInfo findConfigAllInfo(String tpId, String itemId, String tenantId);

    /**
     * Find config recent info.
     *
     * @param params
     * @return
     */
    ConfigAllInfo findConfigRecentInfo(String... params);

    /**
     * Insert or update.
     *
     * @param identify
     * @param isChangeNotice
     * @param configAllInfo
     */
    void insertOrUpdate(String identify, boolean isChangeNotice, ConfigAllInfo configAllInfo);

    /**
     * Register.
     *
     * @param registerWrapper
     */
    void register(DynamicThreadPoolRegisterWrapper registerWrapper);
}

