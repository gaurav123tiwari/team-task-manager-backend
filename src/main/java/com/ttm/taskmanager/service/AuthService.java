package com.ttm.taskmanager.service;

import com.ttm.taskmanager.dto.AuthDto;
import com.ttm.taskmanager.entity.User;
import com.ttm.taskmanager.exception.ResourceNotFoundException;
import com.ttm.taskmanager.repository.UserRepository;
import com.ttm.taskmanager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthDto.AuthResponse signup(AuthDto.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole() != null ? request.getRole() : User.Role.MEMBER)
            .build();

        userRepository.save(user);

        String token = tokenProvider.generateTokenFromEmail(user.getEmail());
        return new AuthDto.AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        return new AuthDto.AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Transactional(readOnly = true)
    public List<AuthDto.UserDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::toUserDto)
            .collect(Collectors.toList());
    }

    public AuthDto.UserDto toUserDto(User user) {
        AuthDto.UserDto dto = new AuthDto.UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
