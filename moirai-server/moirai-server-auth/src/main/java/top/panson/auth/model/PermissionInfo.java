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

package top.panson.auth.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * Permission info.
 */
@Data
@TableName("permission")
public class PermissionInfo {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * role
     */
    private String role;

    /**
     * resource
     */
    private String resource;

    /**
     * action
     */
    private String action;

    /**
     * gmtCreate
     */
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    /**
     * gmtModified
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

    /**
     * delFlag
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
}
