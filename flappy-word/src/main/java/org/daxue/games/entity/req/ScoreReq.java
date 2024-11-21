package org.daxue.games.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoreReq {

    @NotNull(message = "score cannot be null")
    Integer score;

    @NotNull(message = "hurdle cannot be null")
    Integer hurdle;
}
