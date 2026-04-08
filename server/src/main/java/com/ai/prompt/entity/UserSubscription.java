package com.ai.prompt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户订阅记录实体
 *
 * <p>记录用户的付费订阅状态，包括当前套餐、有效期、取消状态等。</p>
 *
 * @author 马可行
 * @since 1.2.0
 */
@Data
@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 套餐ID */
    @Column(name = "plan_id", nullable = false)
    private Long planId;

    /** 订阅状态: ACTIVE, CANCELED, EXPIRED */
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    /** 计费周期: MONTHLY, YEARLY */
    @Column(name = "billing_cycle", length = 20)
    private String billingCycle;

    /** 订阅开始时间 */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /** 订阅到期时间 */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** 支付渠道: ALIPAY, WECHAT, STRIPE */
    @Column(name = "payment_provider", length = 20)
    private String paymentProvider;

    /** 第三方支付订单ID */
    @Column(name = "payment_id", length = 100)
    private String paymentId;

    /** 取消时间 */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    /** 自动续费 */
    @Column(name = "auto_renew")
    private Boolean autoRenew = true;

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

    /**
     * 检查订阅是否有效
     */
    public boolean isValid() {
        if (!"ACTIVE".equals(status)) return false;
        if (expiresAt == null) return true;
        return LocalDateTime.now().isBefore(expiresAt);
    }
}
