package com.lias.lias_backend.member.service;

import com.lias.lias_backend.member.dto.*;
import com.lias.lias_backend.member.entity.*;
import com.lias.lias_backend.member.repository.*;
import com.lias.lias_backend.notification.service.NotificationService;
import com.lias.lias_backend.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final AffiliationRepository affiliationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // 1. Create User
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.MEMBER)
                .status(User.UserStatus.PENDING)
                .build();
        userRepository.save(user);

        // 2. Create linked Member profile
        Member member = Member.builder()
                .user(user)
                .status(Member.MemberStatus.PERMANENT)
                .hireDate(LocalDate.now())
                .build();
        memberRepository.save(member);

        // 3. Create initial affiliation
        Affiliation affiliation = Affiliation.builder()
                .member(member)
                .laboratory("LIAS")
                .startDate(LocalDate.now())
                .build();
        affiliationRepository.save(affiliation);

        // 4. Notify admins/directors of new pending member
        notificationService.notifyNewMemberPending(user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getEmail());
    }
}