package com.payment.api.presentation.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank(message = "멱등성 키는 필수입니다.")
        @Size(max = 100, message = "멱등성 키는 100자 이하여야 합니다.")
        String idempotencyKey,

        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        BigDecimal amount,

        @NotBlank(message = "통화는 필수입니다.")
        String currency,

        @NotBlank(message = "주문명은 필수입니다.")
        @Size(max = 200, message = "주문명은 200자 이하여야 합니다.")
        String orderName
) {}
