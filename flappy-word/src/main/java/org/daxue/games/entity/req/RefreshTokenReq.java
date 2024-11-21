package org.daxue.games.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author：daxue0929
 * @Date：2024/11/21 19:55
 */

@Data
public class RefreshTokenReq {

    @NotBlank(message = "refreshToken cannot be blank")
    String refreshToken;

}
