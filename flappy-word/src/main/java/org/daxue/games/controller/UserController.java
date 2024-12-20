package org.daxue.games.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.daxue.games.controller.base.BaseController;
import org.daxue.games.entity.common.Result;
import org.daxue.games.entity.common.ResultCode;
import org.daxue.games.entity.common.UserStatus;
import org.daxue.games.pojo.User;
import org.daxue.games.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Resource
    UserService userService;

    @PostMapping("/disable/{userId}")
    public Result disableUser(@PathVariable("userId") String userId) throws ParseException {
        User loginUser = getLoginUser();
        // todo login User 需要具备一些权限

        User oldUser = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
        if (oldUser == null) {
            return Result.build(ResultCode.NOT_FOUND_USER);
        }
        if (UserStatus.Disable.equals(oldUser.getStatus())) {
            return Result.build(ResultCode.SUCCESS, "用户已封禁");
        }
        User newUser = oldUser.setStatus(UserStatus.Disable);
        userService.updateById(newUser);
        return Result.buildSuccess();
    }

    @PostMapping("/active/{userId}")
    public Result activeUser(@PathVariable("userId") String userId) throws ParseException {
        User loginUser = getLoginUser();
        // todo login User 需要具备一些权限

        User oldUser = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
        if (oldUser == null) {
            return Result.build(ResultCode.NOT_FOUND_USER);
        }
        if (UserStatus.Active.equals(oldUser.getStatus())) {
            return Result.build(ResultCode.SUCCESS, "用户已激活");
        }
        User newUser = oldUser.setStatus(UserStatus.Active);
        userService.updateById(newUser);
        return Result.buildSuccess();
    }

}
