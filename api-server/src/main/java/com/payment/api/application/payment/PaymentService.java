package com.payment.api.application.payment;

import com.payment.api.domain.member.Member;
import com.payment.api.domain.member.MemberRepository;
import com.payment.api.domain.payment.Payment;
import com.payment.api.domain.payment.PaymentRepository;
import com.payment.api.global.kafka.PaymentApprovedEvent;
import com.payment.api.global.kafka.PaymentEventPublisher;
import com.payment.api.global.redis.RedisLockManager;
import com.payment.api.presentation.payment.dto.PaymentRequest;
import com.payment.api.presentation.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final RedisLockManager redisLockManager;
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public PaymentResponse pay(Long memberId, PaymentRequest request) {
        // 이미 처리된 요청이면 기존 결과 반환 (멱등성)
        return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(PaymentResponse::from)
                .orElseGet(() -> processNewPayment(memberId, request));
    }

    private PaymentResponse processNewPayment(Long memberId, PaymentRequest request) {
        if (!redisLockManager.tryLock(request.idempotencyKey())) {
            throw new IllegalStateException("동일한 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // 락 획득 후 DB 재확인 (이중 체크)
            return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                    .map(PaymentResponse::from)
                    .orElseGet(() -> createAndApprovePayment(memberId, request));
        } finally {
            redisLockManager.unlock(request.idempotencyKey());
        }
    }

    private PaymentResponse createAndApprovePayment(Long memberId, PaymentRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Payment payment = Payment.create(
                member,
                request.idempotencyKey(),
                request.amount(),
                request.currency(),
                request.orderName()
        );
        payment.approve();
        paymentRepository.save(payment);

        paymentEventPublisher.publishApproved(new PaymentApprovedEvent(
                payment.getId(),
                memberId,
                payment.getIdempotencyKey(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getOrderName(),
                payment.getUpdatedAt()
        ));

        log.info("결제 승인 완료. paymentId={}, memberId={}, amount={}",
                payment.getId(), memberId, payment.getAmount());

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse cancel(Long memberId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다."));

        if (!payment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 결제만 취소할 수 있습니다.");
        }

        payment.cancel();
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long memberId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다."));

        if (!payment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 결제만 조회할 수 있습니다.");
        }

        return PaymentResponse.from(payment);
    }
}
