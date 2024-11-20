package org.daxue.games.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.daxue.games.entity.common.Result;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.entity.req.LoginReq;
import org.daxue.games.entity.resp.LoginResp;
import org.daxue.games.pojo.User;
import org.daxue.games.service.UserService;
import org.daxue.games.utils.IDUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    UserService userService;

    @PostMapping
    public Result loginOrReg(@RequestBody @Valid LoginReq req) {
        String username = req.getUsername();
        String code = req.getCode();
        User oldUser = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getName, username));
        if (oldUser != null) {
            if (!oldUser.getCode().equals(code)) {
                Result.build(ResultCode.BAD_REQUEST_CODE);
            }
            return Result.buildSuccess(LoginResp.builder()
                    .userId(oldUser.getUserId())
                    .build()
            );
        }
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        String newUserId = IDUtil.genUserId();
        user.setUserId(newUserId)
                .setCode(code)
                .setName(username)
                .setCreateTime(now)
                .setUpdateTime(now);
        userService.save(user);
        return Result.buildSuccess(
                LoginResp.builder()
                        .userId(newUserId)
                        .build()
        );
    }
}
