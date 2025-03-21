package top.panson.config.service.biz;

import top.panson.common.monitor.Message;
import top.panson.common.monitor.MessageWrapper;
import top.panson.common.web.base.Result;
import top.panson.config.model.HisRunDataInfo;
import top.panson.config.model.biz.monitor.MonitorActiveRespDTO;
import top.panson.config.model.biz.monitor.MonitorQueryReqDTO;
import top.panson.config.model.biz.monitor.MonitorRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * His run data service.
 */
public interface HisRunDataService extends IService<HisRunDataInfo> {

    /**
     * Query.
     *
     * @param reqDTO
     * @return
     */
    List<MonitorRespDTO> query(MonitorQueryReqDTO reqDTO);

    /**
     * Query active thread pool monitor.
     *
     * @param reqDTO
     * @return
     */
    MonitorActiveRespDTO queryInfoThreadPoolMonitor(MonitorQueryReqDTO reqDTO);

    /**
     * Query thread pool last task count.
     *
     * @param reqDTO
     * @return
     */
    MonitorRespDTO queryThreadPoolLastTaskCount(MonitorQueryReqDTO reqDTO);

    /**
     * Save.
     *
     * @param message
     */
    void save(Message message);

    /**
     * dataCollect.
     *
     * @param messageWrapper
     */
    Result<Void> dataCollect(MessageWrapper messageWrapper);
}
