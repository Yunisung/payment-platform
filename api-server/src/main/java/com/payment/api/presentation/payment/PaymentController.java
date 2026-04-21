package com.payment.api.presentation.payment;

import com.payment.api.application.payment.PaymentService;
import com.payment.api.presentation.payment.dto.PaymentRequest;
import com.payment.api.presentation.payment.dto.PaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.pay(memberId, request));
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> cancel(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.cancel(memberId, paymentId));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(memberId, paymentId));
    }
}
