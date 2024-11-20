package org.daxue.games.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.daxue.games.mapper.UserMapper;
import org.daxue.games.pojo.User;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}
