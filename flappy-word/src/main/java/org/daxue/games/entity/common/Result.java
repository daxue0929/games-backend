package org.daxue.games.entity.common;

public class Result<T> {

    private static final String MESSAGE_FORMAT = "%s: %s";

    public Integer code;

    public String message;

    public T data;

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> build(ResultCode resultCode) {
        Result<T> result = new Result<>(resultCode.code, resultCode.getMessage());
        return result;
    }

    public static <T> Result<T> build(ResultCode resultCode, String message) {
        Result<T> result = new Result<>(resultCode.code, message);
        return result;
    }

    public static <T> Result<T> buildSuccess() {
        Result<T> result = new Result<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
        return result;
    }

    public static <T> Result<T> buildSuccess(String message) {
        Result<T> result = new Result<T>(ResultCode.SUCCESS.getCode(), message);
        return result;
    }

    public static <T> Result<T> buildSuccess(T data) {
        Result<T> result = new Result<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
