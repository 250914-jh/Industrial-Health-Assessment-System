package com.industrial.health.controller;

import com.industrial.health.model.User;
import com.industrial.health.repo.UserRepository;
import com.industrial.health.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository u, PasswordEncoder e, JwtUtil j) {
        this.userRepo = u; this.encoder = e; this.jwtUtil = j;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Optional<User> u = userRepo.findByUsername(username);
        if (u.isEmpty() || !encoder.matches(password, u.get().getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = jwtUtil.generate(username);
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("username", username);
        resp.put("role", u.get().getRole());
        return resp;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (userRepo.findByUsername(username).isPresent()) throw new RuntimeException("用户名已存在");
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setRole("USER");
        userRepo.save(u);
        return Map.of("message", "注册成功");
    }
}
