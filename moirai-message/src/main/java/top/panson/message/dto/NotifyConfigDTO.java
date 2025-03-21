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

package top.panson.message.dto;

import top.panson.message.enums.NotifyTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Notify config DTO.
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class NotifyConfigDTO {

    /**
     * Tenant id
     */
    private String tenantId;

    /**
     * Item id
     */
    private String itemId;

    /**
     * Thread-pool id
     */
    private String tpId;

    //通知要发送给的平台
    private String platform;

    //通知类型，也许是config，也就是配置变更通知
    //也许是alarm，那就是告警通知
    private String type;

    //密钥key
    private String secretKey;

    //密钥
    private String secret;

    //通知间隔
    private Integer interval;

    //接收者信息
    private String receives;

    //通知枚举类型
    private NotifyTypeEnum typeEnum;
}
