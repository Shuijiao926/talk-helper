package com.talkhelper.user.controller;

import com.talkhelper.user.dto.ThLoginRequest;
import com.talkhelper.user.dto.ThLoginResponse;
import com.talkhelper.user.dto.ThRegisterRequest;
import com.talkhelper.user.util.ThJwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ThAuthController {

    private final ThJwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    @PostMapping("/login")
    public ThLoginResponse login(@RequestBody ThLoginRequest request) {
        log.info("用户登录: {}", request.getUsername());

        // TODO: 从数据库查询用户并验证密码
        // ThUser user = userService.findByUsername(request.getUsername());
        // if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        //     throw new RuntimeException("用户名或密码错误");
        // }

        String userId = "user-placeholder";
        String username = request.getUsername();

        Map<String, Object> claims = new HashMap<>();
        String token = jwtUtil.generateToken(userId, username, claims);

        return ThLoginResponse.builder()
                .token(token)
                .userId(userId)
                .username(username)
                .expiresIn(expiration / 1000)
                .build();
    }

    @PostMapping("/register")
    public ThLoginResponse register(@RequestBody ThRegisterRequest request) {
        log.info("用户注册: {}", request.getUsername());

        // TODO: 检查用户名是否已存在，创建用户
        // if (userService.existsByUsername(request.getUsername())) {
        //     throw new RuntimeException("用户名已存在");
        // }
        // ThUser user = userService.createUser(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("密码已加密");

        String userId = "user-placeholder";
        String username = request.getUsername();

        Map<String, Object> claims = new HashMap<>();
        String token = jwtUtil.generateToken(userId, username, claims);

        return ThLoginResponse.builder()
                .token(token)
                .userId(userId)
                .username(username)
                .expiresIn(expiration / 1000)
                .build();
    }
}
