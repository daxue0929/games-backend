package org.daxue.games.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.daxue.games.annotation.RequestLimit;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.exception.base.BusinessException;
import org.daxue.games.utils.RedisUtil;
import org.daxue.games.utils.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLimitInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;

    @Autowired
    public RequestLimitInterceptor(RedisConnectionFactory redisConnectionFactory) {
        this.redisUtil = RedisUtil.initialize(redisConnectionFactory);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        RequestLimit methodAnnotation = ((HandlerMethod) handler).getMethodAnnotation(RequestLimit.class);
        RequestLimit classAnnotation = ((HandlerMethod) handler).getBean().getClass().getAnnotation(RequestLimit.class);
        boolean status = true;
        if (methodAnnotation != null) {
            status = validateIpLimit(request, methodAnnotation.count(), methodAnnotation.time());
        }else if (classAnnotation != null){
            status = validateIpLimit(request, classAnnotation.count(), classAnnotation.time());
        }
        if (!status) {
            throw new BusinessException(ResultCode.REQUEST_LIMIT);
        }
        return true;
    }

    private boolean validateIpLimit(HttpServletRequest request, int maxSize, long timeOut) {
        boolean resultCode = true;
        String ip = ServletUtils.getRemoteAddr(request);
        String url = request.getRequestURL().toString();
        String key = "req_limit_".concat(url).concat(ip);
        Integer count = 0;
        Integer cacheCount = redisUtil.get(key, Integer.class);
        if (null == cacheCount) {
            cacheCount = 0;
        }
        count = cacheCount + 1;
        if (count == 1) {
            redisUtil.set(key, count, timeOut);
        } else {
            redisUtil.incr(key, 1);
        }
        log.info("用户IP[{}], 访问地址[{}], 当前次数[{}]", ip, url, count);
        if (count > maxSize) {
            log.warn("用户IP[{}]访问地址[{}]超过了限定的次数[{}]", ip, url, maxSize);
            resultCode = false;
        }
        return resultCode;
    }
}
