package com.ttm.taskmanager.controller;

import com.ttm.taskmanager.dto.AuthDto;
import com.ttm.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthDto.AuthResponse> signup(@Valid @RequestBody AuthDto.SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDto.UserDto> getCurrentUser() {
        return ResponseEntity.ok(authService.toUserDto(authService.getCurrentUser()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<AuthDto.UserDto>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }
}
