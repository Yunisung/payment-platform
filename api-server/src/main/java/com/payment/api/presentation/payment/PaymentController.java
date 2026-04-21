package com.payment.api.presentation.payment;

import com.payment.api.application.payment.PaymentService;
import com.payment.api.global.exception.GlobalExceptionHandler.ErrorResponse;
import com.payment.api.presentation.payment.dto.PaymentRequest;
import com.payment.api.presentation.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "결제", description = "결제 요청/조회/취소 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 요청",
            description = "결제를 요청합니다. idempotencyKey로 중복 요청을 방지합니다.")
    @ApiResponse(responseCode = "201", description = "결제 승인 완료")
    @ApiResponse(responseCode = "400", description = "입력값 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "동일 요청 처리 중 (재시도 필요)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<PaymentResponse> pay(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.pay(memberId, request));
    }

    @Operation(summary = "결제 조회", description = "결제 ID로 결제 내역을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "400", description = "존재하지 않는 결제",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(memberId, paymentId));
    }

    @Operation(summary = "결제 취소", description = "승인된 결제를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "취소 성공")
    @ApiResponse(responseCode = "400", description = "취소 불가 상태",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> cancel(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.cancel(memberId, paymentId));
    }
}
