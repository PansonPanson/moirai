/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.panson.config.service.biz.impl;

import top.panson.common.enums.DelEnum;
import top.panson.common.toolkit.Assert;
import top.panson.common.toolkit.BeanUtil;
import top.panson.common.toolkit.StringUtil;
import top.panson.config.mapper.TenantInfoMapper;
import top.panson.config.model.TenantInfo;
import top.panson.config.model.biz.item.ItemQueryReqDTO;
import top.panson.config.model.biz.item.ItemRespDTO;
import top.panson.config.model.biz.tenant.TenantQueryReqDTO;
import top.panson.config.model.biz.tenant.TenantRespDTO;
import top.panson.config.model.biz.tenant.TenantSaveReqDTO;
import top.panson.config.model.biz.tenant.TenantUpdateReqDTO;
import top.panson.config.service.biz.ItemService;
import top.panson.config.service.biz.TenantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Tenant service impl.
 */
@Service
@AllArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final ItemService itemService;

    private final TenantInfoMapper tenantInfoMapper;

    @Override
    public TenantRespDTO getTenantById(String id) {
        return BeanUtil.convert(tenantInfoMapper.selectById(id), TenantRespDTO.class);
    }

    @Override
    public TenantRespDTO getTenantByTenantId(String tenantId) {
        LambdaQueryWrapper<TenantInfo> queryWrapper = Wrappers
                .lambdaQuery(TenantInfo.class).eq(TenantInfo::getTenantId, tenantId);
        TenantInfo tenantInfo = tenantInfoMapper.selectOne(queryWrapper);
        TenantRespDTO result = BeanUtil.convert(tenantInfo, TenantRespDTO.class);
        return result;
    }

    @Override
    public IPage<TenantRespDTO> queryTenantPage(TenantQueryReqDTO reqDTO) {
        LambdaQueryWrapper<TenantInfo> wrapper = Wrappers.lambdaQuery(TenantInfo.class)
                .eq(StringUtil.isNotEmpty(reqDTO.getTenantId()), TenantInfo::getTenantId, reqDTO.getTenantId())
                .eq(StringUtil.isNotEmpty(reqDTO.getTenantName()), TenantInfo::getTenantName, reqDTO.getTenantName())
                .eq(StringUtil.isNotEmpty(reqDTO.getOwner()), TenantInfo::getOwner, reqDTO.getOwner())
                .orderByDesc(reqDTO.getDesc() != null, TenantInfo::getGmtCreate);
        Page resultPage = tenantInfoMapper.selectPage(reqDTO, wrapper);
        return resultPage.convert(each -> BeanUtil.convert(each, TenantRespDTO.class));
    }

    @Override
    public void saveTenant(TenantSaveReqDTO reqDTO) {
        LambdaQueryWrapper<TenantInfo> queryWrapper = Wrappers.lambdaQuery(TenantInfo.class)
                .eq(TenantInfo::getTenantId, reqDTO.getTenantId());
        // Currently it is a single application, and it supports switching distributed locks during cluster deployment in the future.
        synchronized (TenantService.class) {
            TenantInfo existTenantInfo = tenantInfoMapper.selectOne(queryWrapper);
            Assert.isNull(existTenantInfo, "租户配置已存在.");
            TenantInfo tenantInfo = BeanUtil.convert(reqDTO, TenantInfo.class);
            int insertResult = tenantInfoMapper.insert(tenantInfo);
            boolean retBool = SqlHelper.retBool(insertResult);
            if (!retBool) {
                throw new RuntimeException("Save Error.");
            }
        }
    }

    @Override
    public void updateTenant(TenantUpdateReqDTO reqDTO) {
        TenantInfo tenantInfo = BeanUtil.convert(reqDTO, TenantInfo.class);
        int updateResult = tenantInfoMapper.update(tenantInfo, Wrappers
                .lambdaUpdate(TenantInfo.class).eq(TenantInfo::getTenantId, reqDTO.getTenantId()));
        boolean retBool = SqlHelper.retBool(updateResult);
        if (!retBool) {
            throw new RuntimeException("Update Error.");
        }
    }

    @Override
    public void deleteTenantById(String tenantId) {
        ItemQueryReqDTO reqDTO = new ItemQueryReqDTO();
        reqDTO.setTenantId(tenantId);
        List<ItemRespDTO> itemList = itemService.queryItem(reqDTO);
        if (CollectionUtils.isNotEmpty(itemList)) {
            throw new RuntimeException("租户包含项目引用, 删除失败.");
        }
        int updateResult = tenantInfoMapper.update(new TenantInfo(),
                Wrappers.lambdaUpdate(TenantInfo.class)
                        .eq(TenantInfo::getTenantId, tenantId)
                        .set(TenantInfo::getDelFlag, DelEnum.DELETE.getIntCode()));

        boolean retBool = SqlHelper.retBool(updateResult);
        if (!retBool) {
            throw new RuntimeException("Delete error.");
        }
    }
}
