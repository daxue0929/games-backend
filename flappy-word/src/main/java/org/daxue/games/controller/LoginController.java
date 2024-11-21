package org.daxue.games.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.daxue.games.entity.common.Result;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.entity.req.LoginReq;
import org.daxue.games.entity.resp.LoginResp;
import org.daxue.games.manager.TokenCacheManager;
import org.daxue.games.manager.TokenService;
import org.daxue.games.pojo.User;
import org.daxue.games.service.UserService;
import org.daxue.games.utils.IDUtil;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    UserService userService;

    @Resource
    TokenService tokenService;

    @Resource
    ObjectMapper objectMapper;

    @PostMapping
    public Result loginOrReg(@RequestBody @Valid LoginReq req) throws JOSEException {
        String username = req.getUsername();
        String code = req.getCode();
        User oldUser = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getName, username));
        if (oldUser != null) {
            if (!oldUser.getCode().equals(code)) {
                return Result.build(ResultCode.BAD_REQUEST_CODE);
            }
            Map<String, Object> map = objectMapper.convertValue(oldUser, Map.class);
            String token = tokenService.createJwt(oldUser.getUserId(), map);
            return Result.buildSuccess(LoginResp.builder()
                    .userId(oldUser.getUserId())
                            .token(token)
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
        Map<String, Object> map = objectMapper.convertValue(user, Map.class);
        String token = tokenService.createJwt(newUserId, map);
        return Result.buildSuccess(
                LoginResp.builder()
                        .userId(newUserId)
                        .token(token)
                        .build()
        );
    }
}
