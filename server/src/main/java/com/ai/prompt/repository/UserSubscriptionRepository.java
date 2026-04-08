package com.ai.prompt.repository;

import com.ai.prompt.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户订阅数据访问层
 *
 * @author 马可行
 * @since 1.2.0
 */
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    /**
     * 查找用户当前有效的订阅
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.userId = :userId AND us.status = 'ACTIVE' ORDER BY us.createdAt DESC LIMIT 1")
    Optional<UserSubscription> findActiveByUserId(Long userId);

    /**
     * 查找用户最新一条订阅记录
     */
    Optional<UserSubscription> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
