package com.payment.batch.domain.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("SELECT s FROM Settlement s WHERE s.status = 'PENDING' AND s.paymentApprovedAt < :before")
    List<Settlement> findPendingBefore(@Param("before") LocalDateTime before);
}
