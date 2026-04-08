package com.ai.prompt.controller;

import com.ai.prompt.common.ApiResult;
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

import java.util.Map;

/**
 * 认证接口
 *
 * <p>提供用户注册、登录、当前用户信息查询三大能力。
 * 注册和登录成功后签发 JWT，前端后续请求通过 Authorization 头携带 Token。</p>
 *
 * <h3>认证流程</h3>
 * <ol>
 *   <li>客户端提交用户名 + 密码（注册还需邮箱）</li>
 *   <li>服务端校验参数唯一性（用户名 / 邮箱不可重复）</li>
 *   <li>密码经 BCrypt 哈希后落库（登录时通过 {@code matches} 比对）</li>
 *   <li>返回 JWT + 用户摘要信息</li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 用户注册
     *
     * <p>校验用户名与邮箱的唯一性，通过后对明文密码做 BCrypt 哈希并持久化，
     * 最后签发 JWT 直接完成登录态初始化。</p>
     */
    @Transactional
    @PostMapping("/register")
    public ResponseEntity<ApiResult<?>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册: username={}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResult.badRequest("用户名已存在"));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResult.badRequest("邮箱已被注册"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());

        User saved = userRepository.save(user);
        String token = jwtUtils.generateToken(saved.getUsername());

        log.info("注册成功: userId={}, username={}", saved.getId(), saved.getUsername());
        return ResponseEntity.ok(ApiResult.success(Map.of(
                "token", token,
                "user", buildUserMap(saved)
        )));
    }

    /**
     * 用户登录
     *
     * <p>根据用户名查找记录，BCrypt 比对密码，匹配后签发新 JWT。
     * 登录失败统一返回 401，不暴露具体原因（用户不存在 vs 密码错误使用相同文案）。</p>
     */
    @Transactional(readOnly = true)
    @PostMapping("/login")
    public ResponseEntity<ApiResult<?>> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());

        var userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            log.warn("登录失败-用户不存在: username={}", request.getUsername());
            return ResponseEntity.status(401).body(ApiResult.unauthorized("用户名或密码错误"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败-密码错误: username={}", request.getUsername());
            return ResponseEntity.status(401).body(ApiResult.unauthorized("用户名或密码错误"));
        }

        String token = jwtUtils.generateToken(user.getUsername());
        log.info("登录成功: username={}", request.getUsername());
        return ResponseEntity.ok(ApiResult.success(Map.of(
                "token", token,
                "user", buildUserMap(user)
        )));
    }

    /**
     * 查询当前登录用户信息
     *
     * <p>从请求头提取 Bearer Token，验证有效性后返回用户摘要资料。
     * 匿名访问（无 Authorization 头或 Token 过期/无效）返回 401。</p>
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResult<?>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResult.unauthorized("未登录"));
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(401).body(ApiResult.unauthorized("Token 已过期或无效"));
        }

        String username = jwtUtils.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body(ApiResult.unauthorized("用户不存在"));
        }

        return ResponseEntity.ok(ApiResult.success(buildUserMap(user)));
    }

    /**
     * 更新当前用户资料（昵称 / 头像）
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResult<?>> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody UpdateProfileRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResult.unauthorized("未登录"));
        }
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(401).body(ApiResult.unauthorized("Token 已过期或无效"));
        }

        String username = jwtUtils.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResult.unauthorized("用户不存在"));
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim());
        }

        User saved = userRepository.save(user);
        log.info("资料更新成功: userId={}", saved.getId());

        return ResponseEntity.ok(ApiResult.success(buildUserMap(saved)));
    }

    /* ========== 内部工具 ========== */

    private Map<String, Object> buildUserMap(User user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "nickname", user.getNickname(),
                "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
        );
    }

    /* ========== DTO ========== */

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度 3~50")
        private String username;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度 6~100")
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

    @Data
    public static class UpdateProfileRequest {
        private String nickname;
        private String avatar;
    }
}
