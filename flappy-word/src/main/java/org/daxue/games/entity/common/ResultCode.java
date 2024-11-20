package org.daxue.games.entity.common;

public enum ResultCode {


    BAD_REQUEST(400, "参数错误"),
    BAD_REQUEST_CODE(40001, "code参数错误"),
    NOT_FOUND_USER(40002, "用户不存在"),
    SUCCESS(200, "请求成功"); // 成功

    public int code;
    public String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
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
