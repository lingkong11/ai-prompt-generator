package com.ai.prompt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户配额使用记录实体
 *
 * <p>按日记录用户的资源使用情况，用于配额限制校验。</p>
 *
 * @author 马可行
 * @since 1.2.0
 */
@Data
@Entity
@Table(name = "quota_usage",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "usage_date"}))
public class QuotaUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 使用日期 */
    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    /** 当日生成次数 */
    @Column(name = "generate_count")
    private Integer generateCount = 0;

    /** 当前收藏数量 */
    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

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
     * 增加生成次数
     */
    public void incrementGenerate() {
        this.generateCount++;
    }

    /**
     * 增加收藏数量
     */
    public void incrementFavorite() {
        this.favoriteCount++;
    }

    /**
     * 减少收藏数量
     */
    public void decrementFavorite() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }
}
