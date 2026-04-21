package com.payment.settlement.application;

import com.payment.settlement.domain.Settlement;
import com.payment.settlement.domain.SettlementRepository;
import com.payment.settlement.domain.SettlementStatus;
import com.payment.settlement.infrastructure.kafka.PaymentApprovedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @InjectMocks
    private SettlementService settlementService;

    @Mock
    private SettlementRepository settlementRepository;

    @Test
    @DisplayName("결제 이벤트 수신 시 정산 레코드 생성")
    void createFromPaymentApproved_success() {
        PaymentApprovedEvent event = new PaymentApprovedEvent(
                1L, 10L, "key-001",
                BigDecimal.valueOf(10000), "KRW", "테스트 주문",
                LocalDateTime.now()
        );

        given(settlementRepository.existsByPaymentId(event.paymentId())).willReturn(false);
        given(settlementRepository.save(any(Settlement.class))).willAnswer(i -> i.getArgument(0));

        settlementService.createFromPaymentApproved(event);

        ArgumentCaptor<Settlement> captor = ArgumentCaptor.forClass(Settlement.class);
        verify(settlementRepository).save(captor.capture());

        Settlement saved = captor.getValue();
        assertThat(saved.getPaymentId()).isEqualTo(1L);
        assertThat(saved.getMemberId()).isEqualTo(10L);
        assertThat(saved.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(saved.getStatus()).isEqualTo(SettlementStatus.PENDING);
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 정산 레코드 생성 건너뜀")
    void createFromPaymentApproved_duplicate_skipped() {
        PaymentApprovedEvent event = new PaymentApprovedEvent(
                1L, 10L, "key-001",
                BigDecimal.valueOf(10000), "KRW", "테스트 주문",
                LocalDateTime.now()
        );

        given(settlementRepository.existsByPaymentId(event.paymentId())).willReturn(true);

        settlementService.createFromPaymentApproved(event);

        verify(settlementRepository, never()).save(any());
    }
}
