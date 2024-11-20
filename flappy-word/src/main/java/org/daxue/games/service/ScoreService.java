package org.daxue.games.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.daxue.games.mapper.ScoreMapper;
import org.daxue.games.pojo.Score;
import org.springframework.stereotype.Service;

@Service
public class ScoreService extends ServiceImpl<ScoreMapper, Score> {
}
