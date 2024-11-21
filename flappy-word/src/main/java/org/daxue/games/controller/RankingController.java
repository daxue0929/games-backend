package org.daxue.games.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.daxue.games.entity.common.Result;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.entity.req.ScoreReq;
import org.daxue.games.entity.resp.UserRankResp;
import org.daxue.games.manager.TokenService;
import org.daxue.games.pojo.Score;
import org.daxue.games.pojo.User;
import org.daxue.games.service.ScoreService;
import org.daxue.games.service.UserService;
import org.daxue.games.utils.ServletUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/ranking")
public class RankingController {

    @Resource
    ScoreService scoreService;

    @Resource
    UserService userService;

    @Resource
    TokenService tokenService;

    @PostMapping
    public Result report(@RequestBody @Valid ScoreReq req) throws ParseException {
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
        int number = 0;
        UserRankResp.UserRankRespBuilder builder = UserRankResp.builder().userId(userId);
        for (Score score : list) {
            number++;
            if (score.getUserId().equals(userId)) {
                builder.name(score.getName());
                builder.score(score.getScore());
                builder.hurdle(score.getHurdle());
                break;
            }
        }
        builder.number(number);
        return Result.buildSuccess(builder.build());
    }

}
