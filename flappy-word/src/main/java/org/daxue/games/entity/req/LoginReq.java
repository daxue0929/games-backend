package org.daxue.games.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {

    @NotBlank(message = "name cannot be blank")
    String username;

    @NotBlank(message = "code cannot be blank")
    String code;
}
