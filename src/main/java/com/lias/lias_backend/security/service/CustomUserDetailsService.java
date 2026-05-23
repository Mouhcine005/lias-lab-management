package com.lias.lias_backend.security.service;

import com.lias.lias_backend.member.entity.User;
import com.lias.lias_backend.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        switch (user.getStatus()) {
            case PENDING  -> throw new DisabledException("Your account is pending approval by the director.");
            case FROZEN   -> throw new DisabledException("Your account has been frozen.");
            case DISABLED -> throw new DisabledException("Your account has been disabled.");
            default       -> {}
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}