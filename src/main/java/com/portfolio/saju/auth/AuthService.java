package com.portfolio.saju.auth;

import com.portfolio.saju.auth.dto.AuthResponse;
import com.portfolio.saju.auth.dto.LoginRequest;
import com.portfolio.saju.auth.dto.SignupRequest;
import com.portfolio.saju.common.exception.BusinessException;
import com.portfolio.saju.common.exception.ErrorCode;
import com.portfolio.saju.security.jwt.JwtTokenProvider;
import com.portfolio.saju.user.domain.Role;
import com.portfolio.saju.user.domain.User;
import com.portfolio.saju.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(Role.USER)
                .build();
        userRepository.save(user);

        Authentication authentication = authenticate(request.email(), request.password());
        return AuthResponse.bearer(jwtTokenProvider.createToken(authentication));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticate(request.email(), request.password());
        return AuthResponse.bearer(jwtTokenProvider.createToken(authentication));
    }

    private Authentication authenticate(String email, String password) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException exception) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }
    }
}
