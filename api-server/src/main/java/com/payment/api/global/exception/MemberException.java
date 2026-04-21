package com.payment.api.global.exception;

public class MemberException extends BusinessException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
    }
}
