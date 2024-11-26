package org.daxue.games.manager;

import com.nimbusds.jose.JOSEException;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.logging.SimpleFormatter;


@SpringBootTest
@Slf4j
class TokenServiceTest {

    private static final Long expireSecondToken = 60 * 60L;

    @Resource
    TokenService tokenService;

//    @Test
    void saveToken() throws JOSEException {
        String jwt = tokenService.createJwt("test-sub", expireSecondToken, Map.of("name", "大雪"));
        tokenService.saveToken("test-sub", jwt,  60 * 60L);
        String token = tokenService.getToken("test-sub");
        Assertions.assertEquals(true, StringUtils.isNotBlank(token));
    }

//    @Test
    void createJwt() throws ParseException, JOSEException {
        String jwt = tokenService.createJwt("test-sub", expireSecondToken, Map.of("name", "大雪"));
        log.info(jwt);
        boolean b = tokenService.verifyJwt(jwt);
        Assertions.assertEquals(true, b);
    }
}