package com.payment.settlement.infrastructure.kafka;

import com.payment.settlement.application.SettlementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    @Mock
    private SettlementService settlementService;

    @Test
    @DisplayName("결제 승인 이벤트 수신 시 SettlementService 호출")
    void consumePaymentApproved_callsSettlementService() {
        PaymentApprovedEvent event = new PaymentApprovedEvent(
                1L, 10L, "key-001",
                BigDecimal.valueOf(10000), "KRW", "테스트 주문",
                LocalDateTime.now()
        );

        paymentEventConsumer.consumePaymentApproved(event, 0, 0L);

        verify(settlementService, times(1)).createFromPaymentApproved(event);
    }

    @Test
    @DisplayName("SettlementService 예외 발생 시 예외를 다시 던짐")
    void consumePaymentApproved_whenServiceFails_rethrowsException() {
        PaymentApprovedEvent event = new PaymentApprovedEvent(
                1L, 10L, "key-001",
                BigDecimal.valueOf(10000), "KRW", "테스트 주문",
                LocalDateTime.now()
        );

        org.mockito.BDDMockito.willThrow(new RuntimeException("정산 처리 실패"))
                .given(settlementService).createFromPaymentApproved(event);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> paymentEventConsumer.consumePaymentApproved(event, 0, 0L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("정산 처리 실패");
    }
}
