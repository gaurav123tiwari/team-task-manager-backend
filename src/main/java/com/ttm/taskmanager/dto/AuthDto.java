package com.ttm.taskmanager.dto;

import com.ttm.taskmanager.entity.User.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class SignupRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 100)
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private Role role = Role.MEMBER;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String name;
        private String email;
        private Role role;

        public AuthResponse(String token, Long id, String name, String email, Role role) {
            this.token = token;
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }

    @Data
    public static class UserDto {
        private Long id;
        private String name;
        private String email;
        private Role role;
    }
}
