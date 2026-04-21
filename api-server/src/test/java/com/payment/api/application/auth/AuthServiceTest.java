package com.payment.api.application.auth;

import com.payment.api.domain.member.Member;
import com.payment.api.domain.member.MemberRepository;
import com.payment.api.global.security.JwtTokenProvider;
import com.payment.api.presentation.auth.dto.LoginRequest;
import com.payment.api.presentation.auth.dto.LoginResponse;
import com.payment.api.presentation.auth.dto.SignupRequest;
import com.payment.api.presentation.auth.dto.SignupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import com.payment.api.global.exception.MemberException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        SignupRequest request = new SignupRequest("test@test.com", "password123", "테스터");

        given(memberRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willAnswer(i -> i.getArgument(0));

        SignupResponse response = authService.signup(request);

        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.name()).isEqualTo("테스터");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외 발생")
    void signup_duplicateEmail_throwsException() {
        SignupRequest request = new SignupRequest("test@test.com", "password123", "테스터");

        given(memberRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(MemberException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 성공 시 JWT 반환")
    void login_success() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        Member member = Member.createMerchant("test@test.com", "encodedPassword", "테스터");

        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(true);
        given(jwtTokenProvider.createToken(any(), anyString())).willReturn("jwt.token.here");

        LoginResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt.token.here");
        assertThat(response.email()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
    void login_emailNotFound_throwsException() {
        LoginRequest request = new LoginRequest("wrong@test.com", "password123");

        given(memberRepository.findByEmail(request.email())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호 불일치 시 예외 발생")
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");
        Member member = Member.createMerchant("test@test.com", "encodedPassword", "테스터");

        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
