package top.panson.config.service.biz;

import top.panson.config.model.biz.notify.NotifyListRespDTO;
import top.panson.config.model.biz.notify.NotifyQueryReqDTO;
import top.panson.config.model.biz.notify.NotifyReqDTO;
import top.panson.config.model.biz.notify.NotifyRespDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * Notify service.
 */
public interface NotifyService {

    /**
     * List notify config.
     *
     * @param reqDTO
     * @return
     */
    List<NotifyListRespDTO> listNotifyConfig(NotifyQueryReqDTO reqDTO);

    /**
     * Query page.
     *
     * @param reqDTO
     * @return
     */
    IPage<NotifyRespDTO> queryPage(NotifyQueryReqDTO reqDTO);

    /**
     * Save.
     *
     * @param reqDTO
     */
    void save(NotifyReqDTO reqDTO);

    /**
     * Update.
     *
     * @param reqDTO
     */
    void update(NotifyReqDTO reqDTO);

    /**
     * Save or update.
     *
     * @param notifyUpdateIfExists
     * @param reqDTO
     */
    void saveOrUpdate(boolean notifyUpdateIfExists, NotifyReqDTO reqDTO);

    /**
     * Delete.
     *
     * @param reqDTO
     */
    void delete(NotifyReqDTO reqDTO);

    /**
     * Enable notify.
     *
     * @param id
     * @param status
     */
    void enableNotify(String id, Integer status);
}
