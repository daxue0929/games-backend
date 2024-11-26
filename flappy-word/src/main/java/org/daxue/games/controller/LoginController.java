package org.daxue.games.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.daxue.games.annotation.RequestLimit;
import org.daxue.games.entity.common.Result;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.entity.common.UserStatus;
import org.daxue.games.entity.req.LoginReq;
import org.daxue.games.entity.req.RefreshTokenReq;
import org.daxue.games.entity.resp.LoginResp;
import org.daxue.games.exception.base.BusinessException;
import org.daxue.games.manager.TokenService;
import org.daxue.games.pojo.User;
import org.daxue.games.service.UserService;
import org.daxue.games.utils.IDUtil;

import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Map;

@RequestLimit(count = 20)
@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    private static final Long expireSecondToken = 60 * 60L;
    private static final Long expireSecondRefreshToken = 8 * 60 * 60L;


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
            if (UserStatus.Disable.equals(oldUser.getStatus())) {
                // 禁用状态 关小黑屋
                return Result.build(ResultCode.ACCOUNT_BANNED);
            }
            Map<String, Object> map = objectMapper.convertValue(oldUser, Map.class);
            String token = tokenService.createJwt(oldUser.getUserId(),expireSecondToken, map);
            String refreshToken = tokenService.createJwt(oldUser.getUserId(), expireSecondRefreshToken, null);
            return Result.buildSuccess(LoginResp.builder()
                    .userId(oldUser.getUserId())
                    .token(token)
                    .refreshToken(refreshToken)
                    .build()
            );
        }
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        String newUserId = IDUtil.genUserId();
        user.setUserId(newUserId)
                .setCode(code)
                .setName(username)
                .setStatus(UserStatus.Active)
                .setCreateTime(now)
                .setUpdateTime(now);
        userService.save(user);
        Map<String, Object> map = objectMapper.convertValue(user, Map.class);
        String token = tokenService.createJwt(newUserId,expireSecondToken,  map);
        String refreshToken = tokenService.createJwt(newUserId, expireSecondRefreshToken, null);

        return Result.buildSuccess(
                LoginResp.builder()
                        .userId(newUserId)
                        .token(token)
                        .refreshToken(refreshToken)
                        .build()
        );
    }

    @PostMapping("/refreshToken")
    public Result refreshToken(@RequestBody @Valid RefreshTokenReq req) throws ParseException, JOSEException {
        String refreshToken = req.getRefreshToken();
        try {
            boolean b = tokenService.verifyJwt(refreshToken);
            if (!b) {
                throw new BusinessException(ResultCode.NEXT_LOGIN);
            }
            JWTClaimsSet claims = tokenService.getClaims(refreshToken);
            String userId = claims.getSubject();
            User oldUser = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
            Map<String, Object> map = objectMapper.convertValue(oldUser, Map.class);
            String token = tokenService.createJwt(userId, expireSecondToken, map);
            return Result.buildSuccess(Map.of("token", token));
        } catch (BusinessException businessException) {
            if (ResultCode.UNAUTHORIZED_EXPIRE.getCode().equals(businessException.getCode())) {
                throw new BusinessException(ResultCode.NEXT_LOGIN);
            }
            throw businessException;
        }
    }
}
