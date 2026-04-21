package com.payment.batch.job.settlement;

import com.payment.batch.domain.settlement.Settlement;
import com.payment.batch.domain.settlement.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementItemWriter implements ItemWriter<Settlement> {

    private final SettlementRepository settlementRepository;

    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        settlementRepository.saveAll(chunk.getItems());
        log.info("정산 배치 저장 완료. 건수={}", chunk.size());
    }
}
