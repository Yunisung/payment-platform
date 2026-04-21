package com.payment.settlement.infrastructure.kafka;

import com.payment.settlement.application.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final SettlementService settlementService;

    @KafkaListener(topics = "payment.approved", groupId = "settlement-group")
    public void consumePaymentApproved(
            @Payload PaymentApprovedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("결제 이벤트 수신. paymentId={}, partition={}, offset={}",
                event.paymentId(), partition, offset);

        try {
            settlementService.createFromPaymentApproved(event);
        } catch (Exception e) {
            log.error("정산 처리 실패. paymentId={}", event.paymentId(), e);
            throw e;
        }
    }
}
