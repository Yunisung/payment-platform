package com.payment.api.presentation.auth.dto;

public record SignupResponse(
        Long id,
        String email,
        String name
) {}
