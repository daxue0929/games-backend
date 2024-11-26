package org.daxue.games.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONValue;
import org.daxue.games.annotation.RequestLimit;
import org.daxue.games.entity.common.Result;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.entity.req.ScoreReq;
import org.daxue.games.entity.req.UserAction;
import org.daxue.games.entity.resp.UserRankResp;
import org.daxue.games.manager.TokenService;
import org.daxue.games.pojo.Score;
import org.daxue.games.pojo.User;
import org.daxue.games.service.ScoreService;
import org.daxue.games.service.UserService;
import org.daxue.games.utils.CryptoUtil;
import org.daxue.games.utils.ServletUtils;
import org.daxue.games.validation.GameScoreValidation;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RequestLimit(count = 100)
@Slf4j
@RestController
@RequestMapping("/ranking")
public class RankingController {

    @Resource
    ScoreService scoreService;

    @Resource
    UserService userService;

    @Resource
    TokenService tokenService;

    @Resource
    ObjectMapper objectMapper;

    @PostMapping
    public Result report(@RequestBody @Valid ScoreReq req) throws ParseException {
        String decrypt = CryptoUtil.decrypt(req.getTrack());
        ObjectMapper mapper = new ObjectMapper();
        List<UserAction> parse = null;
        try {
            parse = mapper.readValue(decrypt, new TypeReference<List<UserAction>>(){});
        } catch (JsonProcessingException e) {
            log.error("解析用户行为数据失败", e);
        }
        // 分数校验
        GameScoreValidation validator = GameScoreValidation.builder()
                .actions(parse)
                .reportedScore(req.getScore())
                .reportedThroughCount(req.getHurdle())
                .build();

        boolean isValid = validator.validate();

        if (!isValid) {
            List<String> errors = validator.getValidationErrors();
            log.warn("分数无效原因: \n{}", errors.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
            return Result.build(ResultCode.GAME_INVALID_SCORE);
        }

        String token = ServletUtils.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
        JWTClaimsSet claims = tokenService.getClaims(token);
        String userId = claims.getSubject();
        User currentUser = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
        if (currentUser == null) {
            Result.build(ResultCode.NOT_FOUND_USER);
        }
        Score oldScore = scoreService.getOne(Wrappers.lambdaQuery(Score.class).eq(Score::getUserId, userId));
        if (oldScore != null) {
            if (oldScore.getScore() >= req.getScore()) {
                return Result.buildSuccess("很遗憾~未刷新排名~");
            }
            oldScore.setScore(req.getScore());
            oldScore.setHurdle(req.getHurdle());
            scoreService.updateById(oldScore);
            return Result.buildSuccess("太棒了~刷新排名成功~");
        }
        Score score = new Score();
        LocalDateTime now = LocalDateTime.now();
        score.setUserId(userId)
                .setName(currentUser.getName())
                .setScore(req.getScore())
                .setHurdle(req.getHurdle())
                .setCreateTime(now)
                .setUpdateTime(now);
        scoreService.save(score);
        return Result.buildSuccess("已记录排名~再接再厉哦~");
    }

    @GetMapping
    public Result list(@RequestParam(required = false, defaultValue = "20") Integer pageSize,
                       @RequestParam(required = false, defaultValue = "1") Integer pageNum) {
        Page<Score> page = new Page<>(pageNum, pageSize);
        Page<Score> result = scoreService.page(page, Wrappers.lambdaQuery(Score.class)
                .orderByDesc(Score::getScore)
                .orderByAsc(Score::getCreateTime)
        );
        return Result.buildSuccess(result);
    }

    @GetMapping("/current")
    public Result currentUserRanking() throws ParseException {
        String token = ServletUtils.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
        JWTClaimsSet claims = tokenService.getClaims(token);
        String userId = claims.getSubject();
        List<Score> list = scoreService.list(Wrappers.lambdaQuery(Score.class)
                .orderByDesc(Score::getScore).orderByAsc(Score::getCreateTime)
        );
        Map<String, Object> payload = tokenService.getClaims(token).getClaims();
        User tokenUser = objectMapper.convertValue(payload, User.class);
        int number = 1;
        UserRankResp.UserRankRespBuilder builder = UserRankResp.builder()
                .userId(userId)
                .name(tokenUser.getName());
        Boolean flag = true;
        for (int i = 0; i <list.size(); i++) {
            Score score = list.get(i);
            if (score.getUserId().equals(userId)) {
                flag = false;
                builder.score(score.getScore());
                builder.hurdle(score.getHurdle());
                number = i;
                break;
            }
            number = i;
        }
        if (flag) {
            number = 0;
            builder.score(0);
            builder.hurdle(0);
        }else {
            number = number + 1;
        }
        builder.number(number);
        return Result.buildSuccess(builder.build());
    }

}
