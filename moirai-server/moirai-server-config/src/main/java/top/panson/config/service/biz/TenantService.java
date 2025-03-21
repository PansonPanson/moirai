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

package top.panson.config.service.biz;

import top.panson.config.model.biz.tenant.TenantQueryReqDTO;
import top.panson.config.model.biz.tenant.TenantRespDTO;
import top.panson.config.model.biz.tenant.TenantSaveReqDTO;
import top.panson.config.model.biz.tenant.TenantUpdateReqDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * Tenant service.
 */
public interface TenantService {

    /**
     * Get tenant by id.
     *
     * @param id
     * @return
     */
    TenantRespDTO getTenantById(String id);

    /**
     * Get tenant by tenantId.
     *
     * @param tenantId
     * @return
     */
    TenantRespDTO getTenantByTenantId(String tenantId);

    /**
     * Query tenant page.
     *
     * @param reqDTO
     * @return
     */
    IPage<TenantRespDTO> queryTenantPage(TenantQueryReqDTO reqDTO);

    /**
     * Save tenant.
     *
     * @param reqDTO
     */
    void saveTenant(TenantSaveReqDTO reqDTO);

    /**
     * Update tenant.
     *
     * @param reqDTO
     */
    void updateTenant(TenantUpdateReqDTO reqDTO);

    /**
     * Delete tenant by id.
     *
     * @param tenantId
     */
    void deleteTenantById(String tenantId);
}
