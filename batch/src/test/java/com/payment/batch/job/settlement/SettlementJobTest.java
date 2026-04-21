package com.payment.batch.job.settlement;

import com.payment.batch.domain.settlement.Settlement;
import com.payment.batch.domain.settlement.SettlementRepository;
import com.payment.batch.domain.settlement.SettlementStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class SettlementJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private Job settlementJob;

    @Test
    @DisplayName("PENDING 정산이 배치 실행 후 COMPLETED로 변경됨")
    void settlementJob_completesAllPendingSettlements() throws Exception {
        jobRepositoryTestUtils.removeJobExecutions();
        jobLauncherTestUtils.setJob(settlementJob);

        // 2시간 전 PENDING 정산 3건 저장
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        for (int i = 1; i <= 3; i++) {
            settlementRepository.save(Settlement.fromPaymentApproved(
                    (long) i, 10L, "key-00" + i,
                    BigDecimal.valueOf(10000 * i), "KRW", "주문 " + i,
                    twoHoursAgo
            ));
        }

        JobParameters params = new JobParametersBuilder()
                .addLong("executedAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<Settlement> all = settlementRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all).allMatch(s -> s.getStatus() == SettlementStatus.COMPLETED);
    }

    @Test
    @DisplayName("1시간 이내의 PENDING 정산은 배치 대상에서 제외됨")
    void settlementJob_skipsRecentPendingSettlements() throws Exception {
        jobRepositoryTestUtils.removeJobExecutions();
        jobLauncherTestUtils.setJob(settlementJob);

        // 30분 전 PENDING 정산 (처리 대상 제외)
        settlementRepository.save(Settlement.fromPaymentApproved(
                100L, 10L, "key-recent",
                BigDecimal.valueOf(5000), "KRW", "최근 주문",
                LocalDateTime.now().minusMinutes(30)
        ));

        JobParameters params = new JobParametersBuilder()
                .addLong("executedAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Settlement settlement = settlementRepository.findByPaymentId(100L).orElseThrow();
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
    }

    @Test
    @DisplayName("PENDING 정산이 없으면 배치가 정상 완료됨")
    void settlementJob_withNoPendingSettlements_completesNormally() throws Exception {
        jobRepositoryTestUtils.removeJobExecutions();
        jobLauncherTestUtils.setJob(settlementJob);

        JobParameters params = new JobParametersBuilder()
                .addLong("executedAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}
