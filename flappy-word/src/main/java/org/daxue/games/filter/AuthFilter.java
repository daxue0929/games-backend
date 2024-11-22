package org.daxue.games.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.daxue.games.entity.common.Result;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.exception.base.BusinessException;
import org.daxue.games.manager.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final List<String> WHITELIST_PATHS =
            Arrays.asList("/login", "/login/refreshToken", "/hello");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (isWhitelisted(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.toLowerCase().startsWith("bearer ")) {
            sendErrorResponse(response, ResultCode.SC_UNAUTHORIZED);
            return;
        }
        String jwtToken = getToken(authorization);
        try {
            tokenService.verifyJwt(jwtToken);
        } catch (JOSEException | ParseException  e) {
            sendErrorResponse(response, ResultCode.SC_UNAUTHORIZED, e.getMessage());
            return;
        }catch (BusinessException businessException) {
            sendErrorResponse(response, businessException.getCode(), businessException.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String getToken(String authorization) {
        String token = authorization.substring(7);
        return token;
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private void sendErrorResponse(HttpServletResponse response, Integer resultCode, String message) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.build(resultCode, message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }

    private void sendErrorResponse(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.build(resultCode);
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }

    private void sendErrorResponse(HttpServletResponse response, ResultCode resultCode, String message) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.build(resultCode, message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }
}
