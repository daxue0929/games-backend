package org.daxue.games.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.daxue.games.entity.common.Result;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.entity.req.ScoreReq;
import org.daxue.games.pojo.Score;
import org.daxue.games.pojo.User;
import org.daxue.games.service.ScoreService;
import org.daxue.games.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/ranking")
public class RankingController {

    @Resource
    ScoreService scoreService;

    @Resource
    UserService userService;

    @PostMapping
    public Result report(@RequestBody @Valid ScoreReq req) {
        String userId = req.getUserId();
        User currentUser = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
        if (currentUser == null) {
            Result.build(ResultCode.NOT_FOUND_USER);
        }
        Score score = new Score();
        LocalDateTime now = LocalDateTime.now();
        score.setUserId(req.getUserId())
                .setName(currentUser.getName())
                .setScore(req.getScore())
                .setHurdle(req.getHurdle())
                .setCreateTime(now)
                .setUpdate_time(now);
        scoreService.save(score);
        return Result.buildSuccess(score);
    }

    @GetMapping
    public Result list(@RequestParam(required = false, defaultValue = "20") Integer pageSize,
                       @RequestParam(required = false, defaultValue = "1") Integer pageNum) {
        Page<Score> page = new Page<>(pageNum, pageSize);
        Page<Score> result = scoreService.page(page, Wrappers.lambdaQuery(Score.class)
                .orderByDesc(Score::getScore));
        return Result.buildSuccess(result);
    }
}
