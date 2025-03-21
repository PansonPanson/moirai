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

import top.panson.auth.model.UserInfo;
import top.panson.auth.model.biz.user.UserQueryPageReqDTO;
import top.panson.auth.model.biz.user.UserReqDTO;
import top.panson.auth.model.biz.user.UserRespDTO;
import top.panson.auth.security.AuthManager;
import top.panson.auth.service.UserService;
import top.panson.common.constant.Constants;
import top.panson.common.model.TokenInfo;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static top.panson.auth.constant.Constants.TOKEN_VALIDITY_IN_SECONDS;

/**
 * @Description:处理和用户操作相关的控制器
 */
@RestController
@AllArgsConstructor
@RequestMapping(Constants.BASE_PATH + "/auth/users")
public class UserController {

    private final UserService userService;

    private final AuthManager authManager;

    @PostMapping("/apply/token")
    public Result<TokenInfo> applyToken(@RequestBody UserInfo userInfo) {
        String accessToken = authManager.resolveTokenFromUser(userInfo.getUserName(), userInfo.getPassword());
        TokenInfo tokenInfo = new TokenInfo(accessToken, TOKEN_VALIDITY_IN_SECONDS);
        return Results.success(tokenInfo);
    }

    @PostMapping("/page")
    public Result<IPage<UserRespDTO>> listUser(@RequestBody UserQueryPageReqDTO reqDTO) {
        IPage<UserRespDTO> resultUserPage = userService.listUser(reqDTO);
        return Results.success(resultUserPage);
    }

    @GetMapping("/info/{username}")
    public Result<UserRespDTO> userInfo(@PathVariable("username") String username) {
        UserRespDTO userRespDTO = userService.getUser(new UserReqDTO().setUserName(username));
        return Results.success(userRespDTO);
    }

    @PostMapping("/add")
    public Result<Void> addUser(@RequestBody UserReqDTO reqDTO) {
        userService.addUser(reqDTO);
        return Results.success();
    }

    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody UserReqDTO reqDTO) {
        userService.updateUser(reqDTO);
        return Results.success();
    }

    @DeleteMapping("/remove/{userName}")
    public Result<Void> deleteUser(@PathVariable("userName") String userName) {
        userService.deleteUser(userName);
        return Results.success();
    }

    @GetMapping("/search/{userName}")
    public Result<List<String>> searchUsersLikeUserName(@PathVariable("userName") String userName) {
        List<String> resultUserNames = userService.getUserLikeUsername(userName);
        return Results.success(resultUserNames);
    }
}
