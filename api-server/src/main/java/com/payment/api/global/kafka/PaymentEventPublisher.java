package com.payment.api.global.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private static final String TOPIC = "payment.approved";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishApproved(PaymentApprovedEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.paymentId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("결제 이벤트 발행 실패. paymentId={}", event.paymentId(), ex);
                    } else {
                        log.info("결제 이벤트 발행 완료. paymentId={}", event.paymentId());
                    }
                });
    }
}
