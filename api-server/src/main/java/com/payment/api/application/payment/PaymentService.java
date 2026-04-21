package com.payment.api.application.payment;

import com.payment.api.domain.member.Member;
import com.payment.api.domain.member.MemberRepository;
import com.payment.api.domain.payment.Payment;
import com.payment.api.domain.payment.PaymentRepository;
import com.payment.api.domain.payment.PaymentStatus;
import com.payment.api.global.exception.ErrorCode;
import com.payment.api.global.exception.MemberException;
import com.payment.api.global.exception.PaymentException;
import com.payment.api.global.kafka.PaymentApprovedEvent;
import com.payment.api.global.kafka.PaymentEventPublisher;
import com.payment.api.global.redis.RedisLockManager;
import com.payment.api.presentation.payment.dto.PaymentRequest;
import com.payment.api.presentation.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(PaymentResponse::from)
                .orElseGet(() -> processNewPayment(memberId, request));
    }

    private PaymentResponse processNewPayment(Long memberId, PaymentRequest request) {
        if (!redisLockManager.tryLock(request.idempotencyKey())) {
            throw new PaymentException(ErrorCode.PAYMENT_PROCESSING);
        }

        try {
            return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                    .map(PaymentResponse::from)
                    .orElseGet(() -> createAndApprovePayment(memberId, request));
        } finally {
            redisLockManager.unlock(request.idempotencyKey());
        }
    }

    private PaymentResponse createAndApprovePayment(Long memberId, PaymentRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

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
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getMember().getId().equals(memberId)) {
            throw new PaymentException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        payment.cancel();
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long memberId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getMember().getId().equals(memberId)) {
            throw new PaymentException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(Long memberId, PaymentStatus status, Pageable pageable) {
        Page<Payment> payments = (status != null)
                ? paymentRepository.findByMemberIdAndStatus(memberId, status, pageable)
                : paymentRepository.findByMemberId(memberId, pageable);

        return payments.map(PaymentResponse::from);
    }
}
