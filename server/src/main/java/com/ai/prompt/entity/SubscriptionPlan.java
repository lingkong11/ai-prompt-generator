package com.ai.prompt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅套餐实体
 *
 * <p>定义系统的付费套餐方案，包括价格、功能限制等配置。</p>
 *
 * @author 马可行
 * @since 1.2.0
 */
@Data
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 套餐编码: FREE, PRO, TEAM */
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    /** 套餐名称: 免费版/专业版/团队版 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 月付价格 */
    @Column(name = "price_monthly", precision = 10, scale = 2)
    private BigDecimal priceMonthly;

    /** 年付价格 */
    @Column(name = "price_yearly", precision = 10, scale = 2)
    private BigDecimal priceYearly;

    /** 每日生成次数限制 (-1 表示无限) */
    @Column(name = "generate_limit")
    private Integer generateLimit = -1;

    /** 收藏数量限制 (-1 表示无限) */
    @Column(name = "favorite_limit")
    private Integer favoriteLimit = -1;

    /** 功能特性 JSON: ["export", "priority_support", "api_access"] */
    @Column(name = "features", length = 500)
    private String features;

    /** 是否启用 */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /** 排序权重 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

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
     * 检查套餐是否包含指定功能
     */
    public boolean hasFeature(String feature) {
        if (features == null || features.isEmpty()) return false;
        return features.contains(feature);
    }
}
