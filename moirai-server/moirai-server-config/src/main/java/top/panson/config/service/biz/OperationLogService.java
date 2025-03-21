package top.panson.config.service.biz;

import top.panson.config.model.LogRecordInfo;
import top.panson.config.model.biz.log.LogRecordQueryReqDTO;
import top.panson.config.model.biz.log.LogRecordRespDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * Operation log.
 */
public interface OperationLogService {

    /**
     * Query operation log.
     *
     * @param pageQuery
     * @return
     */
    IPage<LogRecordRespDTO> queryPage(LogRecordQueryReqDTO pageQuery);

    /**
     * Record.
     *
     * @param requestParam
     */
    void record(LogRecordInfo requestParam);
}