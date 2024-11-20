package org.daxue.games.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.daxue.games.entity.common.Result;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.entity.req.LoginReq;
import org.daxue.games.entity.resp.LoginResp;
import org.daxue.games.manager.TokenCacheManager;
import org.daxue.games.pojo.User;
import org.daxue.games.service.UserService;
import org.daxue.games.utils.IDUtil;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    UserService userService;

    @Resource
    TokenCacheManager tokenCacheManager;

    @PostMapping
    public Result loginOrReg(@RequestBody @Valid LoginReq req) {
        String username = req.getUsername();
        String code = req.getCode();
        User oldUser = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getName, username));
        Long expire = Long.valueOf(30 * 60);
        if (oldUser != null) {
            if (!oldUser.getCode().equals(code)) {
                Result.build(ResultCode.BAD_REQUEST_CODE);
            }
            String token = tokenCacheManager.setCache(IDUtil.genUUID(), oldUser.getUserId(), Duration.ofSeconds(expire));
            return Result.buildSuccess(LoginResp.builder()
                    .userId(oldUser.getUserId())
                            .token(token)
                            .expire(expire)
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
        String token = tokenCacheManager.setCache(IDUtil.genUUID(), newUserId, Duration.ofSeconds(expire));
        return Result.buildSuccess(
                LoginResp.builder()
                        .userId(newUserId)
                        .token(token)
                        .expire(expire)
                        .build()
        );
    }
}
