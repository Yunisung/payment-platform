package com.payment.api.application.auth;

import com.payment.api.domain.member.Member;
import com.payment.api.domain.member.MemberRepository;
import com.payment.api.global.exception.ErrorCode;
import com.payment.api.global.exception.MemberException;
import com.payment.api.global.security.JwtTokenProvider;
import com.payment.api.presentation.auth.dto.LoginRequest;
import com.payment.api.presentation.auth.dto.LoginResponse;
import com.payment.api.presentation.auth.dto.SignupRequest;
import com.payment.api.presentation.auth.dto.SignupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.createMerchant(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );
        memberRepository.save(member);

        return new SignupResponse(member.getId(), member.getEmail(), member.getName());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new MemberException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new MemberException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtTokenProvider.createToken(member.getId(), member.getRole().name());
        return new LoginResponse(token, member.getId(), member.getEmail(), member.getRole().name());
    }
}
