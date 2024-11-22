package org.daxue.games.entity.common;

public enum ResultCode {


    BAD_REQUEST(400, "参数错误"),
    SC_UNAUTHORIZED(401, "token无效"),
    BAD_REQUEST_CODE(40001, "校验码错误"),
    NOT_FOUND_USER(40002, "用户不存在"),
    UNAUTHORIZED_EXPIRE(40003, "token已过期,请刷新"),
    NEXT_LOGIN(40004, "请重新登录"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SUCCESS(200, "请求成功"), // 成功

    // 游戏异常
    GAME_INVALID_SCORE(50001, "分数无效"),
    ;

    public int code;
    public String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
