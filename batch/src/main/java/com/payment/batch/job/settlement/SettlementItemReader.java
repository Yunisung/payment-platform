package com.payment.batch.job.settlement;

import com.payment.batch.domain.settlement.Settlement;
import com.payment.batch.domain.settlement.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class SettlementItemReader implements ItemReader<Settlement> {

    private final SettlementRepository settlementRepository;

    private Iterator<Settlement> iterator;

    @Override
    public Settlement read() {
        if (iterator == null) {
            List<Settlement> items = settlementRepository.findPendingBefore(
                    LocalDateTime.now().minusHours(1)
            );
            log.info("처리 대상 정산 건수: {}", items.size());
            iterator = items.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}
