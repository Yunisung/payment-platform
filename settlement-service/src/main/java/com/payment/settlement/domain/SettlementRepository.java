package com.payment.settlement.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByPaymentId(Long paymentId);

    boolean existsByPaymentId(Long paymentId);
}
