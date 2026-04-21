package com.payment.api.presentation.payment.dto;

import com.payment.api.domain.payment.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        String idempotencyKey,
        BigDecimal amount,
        String currency,
        String orderName,
        String status,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getIdempotencyKey(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getOrderName(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );
    }
}
