package com.example.hls_server.service;

import com.example.hls_server.entity.User;
import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.LoginRequest;
import com.example.hls_server.dto.RegisterRequest;
import com.example.hls_server.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.hls_server.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public BaseResponse<UserResponse> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return BaseResponse.<UserResponse>builder()
                    .success(false)
                    .message("Email đã tồn tại")
                    .data(null)
                    .build();
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return BaseResponse.<UserResponse>builder()
                .success(true)
                .message("Đăng ký thành công")
                .data(userResponse)
                .build();
    }

    public BaseResponse<UserResponse> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return BaseResponse.<UserResponse>builder()
                    .success(false)
                    .message("Email không tồn tại")
                    .data(null)
                    .build();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return BaseResponse.<UserResponse>builder()
                    .success(false)
                    .message("Sai mật khẩu")
                    .data(null)
                    .build();
        }

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return BaseResponse.<UserResponse>builder()
                .success(true)
                .message("Đăng nhập thành công")
                .data(userResponse)
                .build();
    }
}
