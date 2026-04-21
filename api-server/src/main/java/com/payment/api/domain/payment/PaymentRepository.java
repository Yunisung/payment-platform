package com.payment.api.domain.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<Payment> findByMemberId(Long memberId, Pageable pageable);

    Page<Payment> findByMemberIdAndStatus(Long memberId, PaymentStatus status, Pageable pageable);
}
