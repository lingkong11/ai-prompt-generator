package com.ai.prompt.repository;

import com.ai.prompt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** 根据用户名查找，用于登录认证和 JWT 验证 */
    Optional<User> findByUsername(String username);

    /** 根据邮箱查找 */
    Optional<User> findByEmail(String email);

    /** 用户名唯一性校验（注册时使用） */
    boolean existsByUsername(String username);

    /** 邮箱唯一性校验（注册时使用） */
    boolean existsByEmail(String email);
}
