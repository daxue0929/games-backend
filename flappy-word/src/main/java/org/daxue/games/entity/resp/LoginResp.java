package org.daxue.games.entity.resp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResp {

    String userId;

    String token;

    String refreshToken;

}
