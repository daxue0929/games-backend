package org.daxue.games.exception.base;

import org.daxue.games.entity.common.ResultCode;

/**
 * @Author：daxue0929
 * @Date：2024/11/21 19:02
 */
public class BusinessException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public Integer code;
    public String message;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
