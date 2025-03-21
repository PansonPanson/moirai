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

package top.panson.config.model.biz.notify;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Notify resp DTO.
 */
@Data
public class NotifyRespDTO {

    /**
     * ID
     */
    private String id;

    /**
     * Ids
     */
    private String ids;

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

    /**
     * Platform
     */
    private String platform;

    /**
     * Type
     */
    private String type;

    /**
     * Config type
     */
    private Boolean configType;

    /**
     * Alarm type
     */
    private Boolean alarmType;

    /**
     * Secret key
     */
    private String secretKey;

    /**
     * Interval
     */
    private Integer interval;

    /**
     * Receives
     */
    private String receives;

    /**
     * Enable
     */
    private Integer enable;

    /**
     * gmtCreate
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtCreate;

    /**
     * gmtModified
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtModified;
}
