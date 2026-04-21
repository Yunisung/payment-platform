package com.payment.settlement.infrastructure.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentApprovedEvent(
        Long paymentId,
        Long memberId,
        String idempotencyKey,
        BigDecimal amount,
        String currency,
        String orderName,
        LocalDateTime approvedAt
) {}
