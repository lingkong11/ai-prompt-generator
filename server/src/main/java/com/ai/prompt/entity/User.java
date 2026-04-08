package com.ai.prompt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 用户实体
 *
 * <p>存储注册用户的账号信息和认证凭据。
 * 密码以 BCrypt Hash 存储，对外序列化时通过 {@code @JsonIgnore} 隐藏。</p>
 * <p>实现 {@link UserDetails} 接口，支持 Spring Security 认证。</p>
 *
 * @see com.ai.prompt.security.JwtUtils
 */
@Data
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录用户名，全局唯一 */
    @Column(unique = true, nullable = false)
    private String username;

    /** 注册邮箱，全局唯一 */
    @Column(unique = true, nullable = false)
    private String email;

    /** BCrypt 哈希后的密码，不参与 JSON 序列化 */
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    /** 用户昵称，注册时若未填写则默认取 username */
    @Column(length = 20)
    private String nickname;

    /** 头像 URL */
    @Column(length = 500)
    private String avatar;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== UserDetails 接口实现 ==========

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
