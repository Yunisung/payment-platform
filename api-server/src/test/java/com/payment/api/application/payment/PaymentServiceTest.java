package com.payment.api.application.payment;

import com.payment.api.domain.member.Member;
import com.payment.api.domain.member.MemberRepository;
import com.payment.api.domain.payment.Payment;
import com.payment.api.domain.payment.PaymentRepository;
import com.payment.api.domain.payment.PaymentStatus;
import com.payment.api.global.kafka.PaymentEventPublisher;
import com.payment.api.global.redis.RedisLockManager;
import com.payment.api.presentation.payment.dto.PaymentRequest;
import com.payment.api.presentation.payment.dto.PaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedisLockManager redisLockManager;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @Test
    @DisplayName("결제 성공 - 신규 요청")
    void pay_success_newRequest() {
        Long memberId = 1L;
        PaymentRequest request = new PaymentRequest("key-001", BigDecimal.valueOf(10000), "KRW", "테스트 주문");
        Member member = Member.createMerchant("test@test.com", "pw", "테스터");

        given(paymentRepository.findByIdempotencyKey(request.idempotencyKey())).willReturn(Optional.empty());
        given(redisLockManager.tryLock(request.idempotencyKey())).willReturn(true);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(paymentRepository.save(any(Payment.class))).willAnswer(i -> i.getArgument(0));

        PaymentResponse response = paymentService.pay(memberId, request);

        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED.name());
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        verify(paymentEventPublisher).publishApproved(any());
        verify(redisLockManager).unlock(request.idempotencyKey());
    }

    @Test
    @DisplayName("결제 멱등성 - 같은 키로 재요청 시 기존 결과 반환")
    void pay_idempotency_returnExistingResult() {
        Long memberId = 1L;
        PaymentRequest request = new PaymentRequest("key-001", BigDecimal.valueOf(10000), "KRW", "테스트 주문");
        Member member = Member.createMerchant("test@test.com", "pw", "테스터");
        Payment existing = Payment.create(member, "key-001", BigDecimal.valueOf(10000), "KRW", "테스트 주문");
        existing.approve();

        given(paymentRepository.findByIdempotencyKey(request.idempotencyKey())).willReturn(Optional.of(existing));

        PaymentResponse response = paymentService.pay(memberId, request);

        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED.name());
        verify(redisLockManager, never()).tryLock(any());
        verify(paymentEventPublisher, never()).publishApproved(any());
    }

    @Test
    @DisplayName("Redis 락 획득 실패 시 예외 발생")
    void pay_lockFailed_throwsException() {
        Long memberId = 1L;
        PaymentRequest request = new PaymentRequest("key-001", BigDecimal.valueOf(10000), "KRW", "테스트 주문");

        given(paymentRepository.findByIdempotencyKey(request.idempotencyKey())).willReturn(Optional.empty());
        given(redisLockManager.tryLock(request.idempotencyKey())).willReturn(false);

        assertThatThrownBy(() -> paymentService.pay(memberId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("동일한 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    @DisplayName("결제 취소 성공")
    void cancel_success() {
        Long memberId = 1L;
        Long paymentId = 100L;
        Member member = Member.createMerchant("test@test.com", "pw", "테스터");
        ReflectionTestUtils.setField(member, "id", memberId);

        Payment payment = Payment.create(member, "key-001", BigDecimal.valueOf(10000), "KRW", "테스트 주문");
        payment.approve();

        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        PaymentResponse response = paymentService.cancel(memberId, paymentId);

        assertThat(response.status()).isEqualTo(PaymentStatus.CANCELLED.name());
    }

    @Test
    @DisplayName("존재하지 않는 결제 취소 시 예외 발생")
    void cancel_notFound_throwsException() {
        given(paymentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.cancel(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 결제입니다.");
    }
}
