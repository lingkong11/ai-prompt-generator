package com.ai.prompt.security;

import com.ai.prompt.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 *
 * <p>每个 HTTP 请求经过此过滤器，执行流程如下：
 * <ol>
 *   <li>从 {@code Authorization} 请求头提取 Bearer Token</li>
 *   <li>调用 {@link JwtUtils#validateToken} 校验签名和有效期</li>
 *   <li>解析用户名 → 查库获取完整 User 实体</li>
 *   <li>将认证信息写入 {@link SecurityContextHolder}，后续 Controller 通过
 *       {@code Authentication.getPrincipal()} 获取当前用户</li>
 *   <li>Token 无效或不存在时跳过，交由 Spring Security 后续链路处理（通常返回 401）</li>
 * </ol>
 *
 * <p>注意：此过滤器不处理登录/注册请求，这些路径在 {@code SecurityConfig} 中已配置 permitAll()。</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 仅处理 Bearer Token 格式的请求头
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);

                // 查库获取完整用户实体，写入 SecurityContext
                userRepository.findByUsername(username).ifPresent(user -> {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}
