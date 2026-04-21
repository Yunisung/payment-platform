package com.payment.api.domain.payment;

import com.payment.api.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_idempotency_key", columnList = "idempotencyKey", unique = true),
        @Index(name = "idx_payments_member_id", columnList = "member_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String orderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Payment(Member member, String idempotencyKey, BigDecimal amount,
                    String currency, String orderName) {
        this.member = member;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.currency = currency;
        this.orderName = orderName;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Payment create(Member member, String idempotencyKey,
                                 BigDecimal amount, String currency, String orderName) {
        return Payment.builder()
                .member(member)
                .idempotencyKey(idempotencyKey)
                .amount(amount)
                .currency(currency)
                .orderName(orderName)
                .build();
    }

    public void approve() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 결제만 승인할 수 있습니다.");
        }
        this.status = PaymentStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status != PaymentStatus.APPROVED) {
            throw new IllegalStateException("APPROVED 상태의 결제만 취소할 수 있습니다.");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
}
