package top.panson.config.service.biz;

import top.panson.config.model.biz.item.ItemQueryReqDTO;
import top.panson.config.model.biz.item.ItemRespDTO;
import top.panson.config.model.biz.item.ItemSaveReqDTO;
import top.panson.config.model.biz.item.ItemUpdateReqDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * Item service.
 */
public interface ItemService {

    /**
     * Query item page.
     *
     * @param reqDTO
     * @return
     */
    IPage<ItemRespDTO> queryItemPage(ItemQueryReqDTO reqDTO);

    /**
     * Query item by id.
     *
     * @param tenantId
     * @param itemId
     * @return
     */
    ItemRespDTO queryItemById(String tenantId, String itemId);

    /**
     * Query item.
     *
     * @param reqDTO
     * @return
     */
    List<ItemRespDTO> queryItem(ItemQueryReqDTO reqDTO);

    /**
     * Save item.
     *
     * @param reqDTO
     */
    void saveItem(ItemSaveReqDTO reqDTO);

    /**
     * Update item.
     *
     * @param reqDTO
     */
    void updateItem(ItemUpdateReqDTO reqDTO);

    /**
     * Delete item.
     *
     * @param tenantId
     * @param itemId
     */
    void deleteItem(String tenantId, String itemId);
}
