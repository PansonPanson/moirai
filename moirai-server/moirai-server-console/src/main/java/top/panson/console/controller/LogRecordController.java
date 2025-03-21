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
import top.panson.config.model.biz.log.LogRecordQueryReqDTO;
import top.panson.config.model.biz.log.LogRecordRespDTO;
import top.panson.config.service.biz.OperationLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:处理查询日志记录信息请求，这个控制器目前还用不到大家不必关心
 */
@RestController
@AllArgsConstructor
@RequestMapping(Constants.BASE_PATH + "/log")
public class LogRecordController {

    private final OperationLogService operationLogService;

    @PostMapping("/query/page")
    public Result<IPage<LogRecordRespDTO>> queryPage(@RequestBody LogRecordQueryReqDTO queryReqDTO) {
        return Results.success(operationLogService.queryPage(queryReqDTO));
    }
}
