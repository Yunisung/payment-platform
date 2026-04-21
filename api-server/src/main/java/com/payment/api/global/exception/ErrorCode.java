package com.payment.api.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 회원
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "M001", "이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "M002", "존재하지 않는 회원입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "M003", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 결제
    PAYMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "P001", "존재하지 않는 결제입니다."),
    PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "본인의 결제만 접근할 수 있습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "P003", "현재 상태에서 처리할 수 없는 요청입니다."),
    PAYMENT_PROCESSING(HttpStatus.CONFLICT, "P004", "동일한 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
