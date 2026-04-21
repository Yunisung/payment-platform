package com.payment.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.api.application.auth.AuthService;
import com.payment.api.presentation.auth.dto.LoginRequest;
import com.payment.api.presentation.auth.dto.SignupRequest;
import com.payment.api.presentation.payment.dto.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MockKafkaConfig.class)
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    private String accessToken;

    @BeforeEach
    void setUp() {
        authService.signup(new SignupRequest("pay@test.com", "password123", "결제테스터"));
        var loginResponse = authService.login(new LoginRequest("pay@test.com", "password123"));
        accessToken = loginResponse.accessToken();
    }

    @Test
    @DisplayName("결제 요청 성공")
    void pay_success() throws Exception {
        PaymentRequest request = new PaymentRequest(
                "unique-key-001", BigDecimal.valueOf(10000), "KRW", "테스트 주문");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.amount").value(10000));
    }

    @Test
    @DisplayName("같은 멱등성 키로 중복 요청 시 동일한 결과 반환")
    void pay_duplicateIdempotencyKey_returnsSameResult() throws Exception {
        PaymentRequest request = new PaymentRequest(
                "unique-key-002", BigDecimal.valueOf(10000), "KRW", "테스트 주문");

        String firstResponse = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // 동일한 paymentId 반환
        var first = objectMapper.readTree(firstResponse);
        var second = objectMapper.readTree(secondResponse);
        org.assertj.core.api.Assertions.assertThat(first.get("paymentId").asLong())
                .isEqualTo(second.get("paymentId").asLong());
    }

    @Test
    @DisplayName("결제 조회 성공")
    void getPayment_success() throws Exception {
        PaymentRequest request = new PaymentRequest(
                "unique-key-003", BigDecimal.valueOf(5000), "KRW", "조회 테스트");

        String response = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long paymentId = objectMapper.readTree(response).get("paymentId").asLong();

        mockMvc.perform(get("/api/payments/" + paymentId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId));
    }

    @Test
    @DisplayName("인증 없이 결제 요청 시 403 반환")
    void pay_withoutAuth_returns403() throws Exception {
        PaymentRequest request = new PaymentRequest(
                "unique-key-004", BigDecimal.valueOf(10000), "KRW", "테스트 주문");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("음수 금액으로 결제 요청 시 400 반환")
    void pay_negativeAmount_returns400() throws Exception {
        PaymentRequest request = new PaymentRequest(
                "unique-key-005", BigDecimal.valueOf(-1000), "KRW", "테스트 주문");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("결제 목록 조회 성공 - 페이징 응답 반환")
    void getPayments_success() throws Exception {
        // 결제 2건 생성
        for (int i = 1; i <= 2; i++) {
            mockMvc.perform(post("/api/payments")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new PaymentRequest("list-key-00" + i, BigDecimal.valueOf(1000 * i), "KRW", "주문 " + i))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/payments")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("결제 목록 상태 필터 조회 - APPROVED만 반환")
    void getPayments_filterByStatus_returnsOnlyMatching() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PaymentRequest("filter-key-001", BigDecimal.valueOf(5000), "KRW", "필터 테스트"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/payments")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("APPROVED"));
    }
}
