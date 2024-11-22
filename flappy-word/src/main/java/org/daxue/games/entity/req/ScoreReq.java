package org.daxue.games.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ScoreReq {

    @NotNull(message = "score cannot be null")
    Integer score;

    @NotNull(message = "hurdle cannot be null")
    Integer hurdle;

    List<UserAction> userActions;
}
