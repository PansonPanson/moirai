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

package top.panson.console.controller;

import top.panson.common.constant.Constants;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.config.model.biz.tenant.TenantQueryReqDTO;
import top.panson.config.model.biz.tenant.TenantRespDTO;
import top.panson.config.model.biz.tenant.TenantSaveReqDTO;
import top.panson.config.model.biz.tenant.TenantUpdateReqDTO;
import top.panson.config.service.biz.TenantService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Description:这个类处理web界面查询tenant信息的请求，这里我把这个类引入进来，也是因为web界面的条件查询的请求必须要处理
 * 否则没办法从服务端获取存储到数据库的线程池信息
 */
@RestController
@AllArgsConstructor
@RequestMapping(Constants.BASE_PATH + "/tenant")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/query/page")
    public Result<IPage<TenantRespDTO>> queryNameSpacePage(@RequestBody TenantQueryReqDTO reqDTO) {
        IPage<TenantRespDTO> resultPage = tenantService.queryTenantPage(reqDTO);
        return Results.success(resultPage);
    }

    @GetMapping("/query/{tenantId}")
    public Result<TenantRespDTO> queryNameSpace(@PathVariable("tenantId") String tenantId) {
        return Results.success(tenantService.getTenantByTenantId(tenantId));
    }

    @PostMapping("/save")
    public Result<Boolean> saveNameSpace(@Validated @RequestBody TenantSaveReqDTO reqDTO) {
        tenantService.saveTenant(reqDTO);
        return Results.success(Boolean.TRUE);
    }

    @PostMapping("/update")
    public Result<Boolean> updateNameSpace(@RequestBody TenantUpdateReqDTO reqDTO) {
        tenantService.updateTenant(reqDTO);
        return Results.success(Boolean.TRUE);
    }

    @DeleteMapping("/delete/{tenantId}")
    public Result<Boolean> deleteNameSpace(@PathVariable("tenantId") String tenantId) {
        tenantService.deleteTenantById(tenantId);
        return Results.success(Boolean.TRUE);
    }
}

