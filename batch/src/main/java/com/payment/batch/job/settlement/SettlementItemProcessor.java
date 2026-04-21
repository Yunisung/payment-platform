package com.payment.batch.job.settlement;

import com.payment.batch.domain.settlement.Settlement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementItemProcessor implements ItemProcessor<Settlement, Settlement> {

    @Override
    public Settlement process(Settlement settlement) {
        settlement.complete();
        log.debug("정산 완료 처리. settlementId={}, amount={}", settlement.getId(), settlement.getAmount());
        return settlement;
    }
}
