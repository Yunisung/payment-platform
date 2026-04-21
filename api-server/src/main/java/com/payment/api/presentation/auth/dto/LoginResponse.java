package com.payment.api.presentation.auth.dto;

public record LoginResponse(
        String accessToken,
        Long memberId,
        String email,
        String role
) {}
