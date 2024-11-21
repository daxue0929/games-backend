package org.daxue.games.entity.resp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRankResp {

    String userId;

    String name;

    int number;

    Integer score;

    Integer hurdle;
}
