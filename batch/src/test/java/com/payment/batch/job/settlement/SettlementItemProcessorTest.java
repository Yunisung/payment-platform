package com.payment.batch.job.settlement;

import com.payment.batch.domain.settlement.Settlement;
import com.payment.batch.domain.settlement.SettlementStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementItemProcessorTest {

    private final SettlementItemProcessor processor = new SettlementItemProcessor();

    @Test
    @DisplayName("PENDING 정산을 처리하면 COMPLETED 상태로 변경")
    void process_pendingSettlement_completesIt() throws Exception {
        Settlement settlement = Settlement.fromPaymentApproved(
                1L, 10L, "key-001",
                BigDecimal.valueOf(10000), "KRW", "테스트 주문",
                LocalDateTime.now()
        );

        Settlement result = processor.process(settlement);

        assertThat(result).isSameAs(settlement);
        assertThat(result.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
    }
}
