package com.example.hls_server.controller;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.LoginRequest;
import com.example.hls_server.dto.RegisterRequest;
import com.example.hls_server.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.hls_server.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public BaseResponse<UserResponse> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public BaseResponse<UserResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}