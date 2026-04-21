package com.payment.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements", indexes = {
        @Index(name = "idx_settlements_payment_id", columnList = "paymentId", unique = true),
        @Index(name = "idx_settlements_member_id", columnList = "memberId"),
        @Index(name = "idx_settlements_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long paymentId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String orderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @Column(nullable = false)
    private LocalDateTime paymentApprovedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Settlement(Long paymentId, Long memberId, String idempotencyKey,
                       BigDecimal amount, String currency, String orderName,
                       LocalDateTime paymentApprovedAt) {
        this.paymentId = paymentId;
        this.memberId = memberId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.currency = currency;
        this.orderName = orderName;
        this.status = SettlementStatus.PENDING;
        this.paymentApprovedAt = paymentApprovedAt;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Settlement fromPaymentApproved(Long paymentId, Long memberId, String idempotencyKey,
                                                  BigDecimal amount, String currency, String orderName,
                                                  LocalDateTime paymentApprovedAt) {
        return Settlement.builder()
                .paymentId(paymentId)
                .memberId(memberId)
                .idempotencyKey(idempotencyKey)
                .amount(amount)
                .currency(currency)
                .orderName(orderName)
                .paymentApprovedAt(paymentApprovedAt)
                .build();
    }

    public void complete() {
        this.status = SettlementStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
}
