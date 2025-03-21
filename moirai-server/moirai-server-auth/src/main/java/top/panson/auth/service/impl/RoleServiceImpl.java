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

package top.panson.auth.service.impl;

import top.panson.auth.mapper.RoleMapper;
import top.panson.auth.model.biz.role.RoleQueryPageReqDTO;
import top.panson.auth.model.biz.role.RoleRespDTO;
import top.panson.auth.service.PermissionService;
import top.panson.auth.service.RoleService;
import top.panson.common.toolkit.BeanUtil;
import top.panson.common.toolkit.CollectionUtil;
import top.panson.common.toolkit.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import top.panson.auth.model.RoleInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role service impl.
 */
@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    private final PermissionService permissionService;

    @Override
    public IPage<RoleRespDTO> listRole(int pageNo, int pageSize) {
        RoleQueryPageReqDTO queryPage = new RoleQueryPageReqDTO(pageNo, pageSize);
        IPage<RoleInfo> selectPage = roleMapper.selectPage(queryPage, null);
        return selectPage.convert(each -> BeanUtil.convert(each, RoleRespDTO.class));
    }

    @Override
    public void addRole(String role, String userName) {
        LambdaQueryWrapper<RoleInfo> queryWrapper = Wrappers.lambdaQuery(RoleInfo.class)
                .eq(RoleInfo::getRole, role);
        RoleInfo roleInfo = roleMapper.selectOne(queryWrapper);
        if (roleInfo != null) {
            throw new RuntimeException("角色名重复");
        }
        RoleInfo insertRole = new RoleInfo();
        insertRole.setRole(role);
        insertRole.setUserName(userName);
        roleMapper.insert(insertRole);
    }

    @Override
    public void deleteRole(String role, String userName) {
        List<String> roleStrList = CollectionUtil.toList(role);
        if (StringUtil.isBlank(role)) {
            LambdaQueryWrapper<RoleInfo> queryWrapper = Wrappers.lambdaQuery(RoleInfo.class).eq(RoleInfo::getUserName, userName);
            roleStrList = roleMapper.selectList(queryWrapper).stream().map(RoleInfo::getRole).collect(Collectors.toList());
        }
        LambdaUpdateWrapper<RoleInfo> updateWrapper = Wrappers.lambdaUpdate(RoleInfo.class)
                .eq(StringUtil.isNotBlank(role), RoleInfo::getRole, role)
                .eq(StringUtil.isNotBlank(userName), RoleInfo::getUserName, userName);
        roleMapper.delete(updateWrapper);
        roleStrList.forEach(each -> permissionService.deletePermission(each, "", ""));
    }

    @Override
    public List<String> getRoleLike(String role) {
        LambdaQueryWrapper<RoleInfo> queryWrapper = Wrappers.lambdaQuery(RoleInfo.class)
                .like(RoleInfo::getRole, role)
                .select(RoleInfo::getRole);
        List<RoleInfo> roleInfos = roleMapper.selectList(queryWrapper);
        List<String> roleNames = roleInfos.stream().map(RoleInfo::getRole).collect(Collectors.toList());
        return roleNames;
    }
}
