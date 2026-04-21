package com.payment.settlement.application;

import com.payment.settlement.domain.Settlement;
import com.payment.settlement.domain.SettlementRepository;
import com.payment.settlement.infrastructure.kafka.PaymentApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    @Transactional
    public void createFromPaymentApproved(PaymentApprovedEvent event) {
        // 중복 이벤트 방어 (Kafka at-least-once 보장)
        if (settlementRepository.existsByPaymentId(event.paymentId())) {
            log.warn("이미 처리된 결제 이벤트. paymentId={}", event.paymentId());
            return;
        }

        Settlement settlement = Settlement.fromPaymentApproved(
                event.paymentId(),
                event.memberId(),
                event.idempotencyKey(),
                event.amount(),
                event.currency(),
                event.orderName(),
                event.approvedAt()
        );
        settlementRepository.save(settlement);

        log.info("정산 레코드 생성 완료. settlementId={}, paymentId={}, amount={}",
                settlement.getId(), event.paymentId(), event.amount());
    }
}
