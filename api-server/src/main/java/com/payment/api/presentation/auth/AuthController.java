package com.payment.api.presentation.auth;

import com.payment.api.application.auth.AuthService;
import com.payment.api.presentation.auth.dto.LoginRequest;
import com.payment.api.presentation.auth.dto.LoginResponse;
import com.payment.api.presentation.auth.dto.SignupRequest;
import com.payment.api.presentation.auth.dto.SignupResponse;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
