package com.payment.batch.job.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.payment.batch.domain.settlement.Settlement;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private static final int CHUNK_SIZE = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SettlementItemReader settlementItemReader;
    private final SettlementItemProcessor settlementItemProcessor;
    private final SettlementItemWriter settlementItemWriter;

    @Bean
    public Job settlementJob() {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep())
                .build();
    }

    @Bean
    public Step settlementStep() {
        return new StepBuilder("settlementStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(settlementItemReader)
                .processor(settlementItemProcessor)
                .writer(settlementItemWriter)
                .build();
    }
}
