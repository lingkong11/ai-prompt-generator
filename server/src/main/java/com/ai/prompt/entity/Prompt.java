package com.ai.prompt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词实体
 *
 * <p>用户生成并保存的完整提示词，关联用户和可选分类。
 * 支持收藏标记、模板标记和使用次数统计。</p>
 */
@Data
@Entity
@Table(name = "prompts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Prompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 提示词标题 */
    @Column(nullable = false)
    private String title;

    /** 提示词正文内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 类型：writing / coding / analysis / agent / general */
    @Column(length = 50)
    private String type;

    /** 文风描述 */
    @Column(length = 50)
    private String style;

    /** 输出语言：zh / en */
    @Column(length = 10)
    private String language;

    /** 生成目标描述 */
    @Column(length = 100)
    private String goal;

    /** 所属用户 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    /** 可选分类 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** 逗号分隔的标签 */
    @Column(length = 200)
    private String tags;

    /** 是否已收藏 */
    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    /** 是否设为模板 */
    @Column(name = "is_template")
    private Boolean isTemplate = false;

    /** 使用次数（每次点击「使用」递增） */
    @Column(name = "use_count")
    private Integer useCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isFavorite == null) this.isFavorite = false;
        if (this.isTemplate == null) this.isTemplate = false;
        if (this.useCount == null) this.useCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
