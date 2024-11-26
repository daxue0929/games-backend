package org.daxue.games.controller.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.annotation.Resource;
import org.daxue.games.manager.TokenService;
import org.daxue.games.pojo.User;
import org.daxue.games.utils.ServletUtils;
import org.springframework.http.HttpHeaders;

import java.text.ParseException;
import java.util.Map;

public abstract class BaseController {

    @Resource
    public TokenService tokenService;

    @Resource
    ObjectMapper objectMapper;

    public User getLoginUser() throws ParseException {
        String token = ServletUtils.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
        JWTClaimsSet claims = tokenService.getClaims(token);
        Map<String, Object> payload = claims.getClaims();
        User tokenUser = objectMapper.convertValue(payload, User.class);
        return tokenUser;
    }


}
