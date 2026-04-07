package com.ai.prompt.controller;

import com.ai.prompt.entity.User;
import com.ai.prompt.repository.UserRepository;
import com.ai.prompt.security.JwtUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    @Transactional
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        log.info("[时间戳:{}] [阶段:Controller] [任务:用户注册] [动作:register] - username:{}", timestamp, request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名已存在"));
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "邮箱已被注册"));
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        
        log.info("[时间戳:{}] [阶段:Controller] [任务:保存用户] - username:{} passwordHash:{}", timestamp, user.getUsername(), encodedPassword.substring(0, 30) + "...");
        
        User savedUser = userRepository.save(user);
        
        log.info("[时间戳:{}] [阶段:Controller] [任务:用户已保存] - userId:{} username:{}", timestamp, savedUser.getId(), savedUser.getUsername());
        
        // 验证保存
        User verifyUser = userRepository.findById(savedUser.getId()).orElse(null);
        log.info("[时间戳:{}] [阶段:Controller] [任务:验证保存] - found:{} passwordHash:{}", 
            timestamp, verifyUser != null, verifyUser != null ? verifyUser.getPassword().substring(0, 30) + "..." : "null");
        
        String token = jwtUtils.generateToken(user.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", timestamp);
        response.put("status", "success");
        response.put("message", "注册成功");
        response.put("token", token);
        response.put("user", Map.of(
            "id", savedUser.getId(),
            "username", savedUser.getUsername(),
            "email", savedUser.getEmail(),
            "nickname", savedUser.getNickname()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @Transactional(readOnly = true)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        log.info("[时间戳:{}] [阶段:Controller] [任务:用户登录] [动作:login] - username:{}", timestamp, request.getUsername());
        
        try {
            // 检查userRepository是否注入
            log.info("[时间戳:{}] [阶段:Controller] [任务:检查Repository] - userRepository:{}", timestamp, userRepository != null ? "已注入" : "null");
            
            // 查询所有用户
            var allUsers = userRepository.findAll();
            log.info("[时间戳:{}] [阶段:Controller] [任务:所有用户] - count:{} users:{}", timestamp, allUsers.size(), allUsers);
            
            // 直接验证用户
            var userOpt = userRepository.findByUsername(request.getUsername());
            log.info("[时间戳:{}] [阶段:Controller] [任务:查找用户] - found:{}", timestamp, userOpt.isPresent());
            
            if (userOpt.isEmpty()) {
                log.warn("[时间戳:{}] [阶段:Controller] [任务:用户不存在] - username:{}", timestamp, request.getUsername());
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在", "username", request.getUsername()));
            }
            
            User user = userOpt.get();
            
            String rawPassword = request.getPassword();
            String encodedPassword = user.getPassword();
            boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
            
            log.info("[时间戳:{}] [阶段:Controller] [任务:密码验证] - matches:{}", timestamp, matches);
            
            // 验证密码
            if (!matches) {
                log.warn("[时间戳:{}] [阶段:Controller] [任务:密码错误] - username:{}", timestamp, request.getUsername());
                return ResponseEntity.status(401).body(Map.of("error", "密码错误"));
            }
            
            String token = jwtUtils.generateToken(request.getUsername());
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("nickname", user.getNickname());
            userMap.put("avatar", user.getAvatar());
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", timestamp);
            response.put("status", "success");
            response.put("token", token);
            response.put("user", userMap);
            
            log.info("[时间戳:{}] [阶段:Controller] [任务:登录成功] - username:{}", timestamp, request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[时间戳:{}] [阶段:Controller] [任务:登录异常] [动作:error] - username:{} error:{}", timestamp, request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(401).body(Map.of("error", "登录失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "token无效"));
        }
        
        String username = jwtUtils.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
        }
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("nickname", user.getNickname());
        userMap.put("avatar", user.getAvatar());
        return ResponseEntity.ok(userMap);
    }
    
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度3-50")
        private String username;
        
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度6-100")
        private String password;
        
        private String nickname;
    }
    
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        private String password;
    }
}
