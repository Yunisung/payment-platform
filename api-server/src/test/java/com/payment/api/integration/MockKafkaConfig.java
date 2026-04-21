package com.payment.api.integration;

import com.payment.api.global.kafka.PaymentEventPublisher;
import com.payment.api.global.redis.RedisLockManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class MockKafkaConfig {

    @Bean
    @Primary
    public PaymentEventPublisher paymentEventPublisher() {
        return mock(PaymentEventPublisher.class);
    }

    @Bean
    @Primary
    public RedisLockManager redisLockManager() {
        RedisLockManager mockLock = mock(RedisLockManager.class);
        when(mockLock.tryLock(anyString())).thenReturn(true);
        return mockLock;
    }
}
