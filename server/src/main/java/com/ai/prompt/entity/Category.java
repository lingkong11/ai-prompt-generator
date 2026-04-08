package com.ai.prompt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分类实体
 *
 * <p>提示词的分类目录，支持层级关系（通过 parentId 实现简单的树形结构）。
 * 每个分类按 sortOrder 升序排列。</p>
 */
@Data
@Entity
@Table(name = "categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分类名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 分类描述 */
    @Column(length = 200)
    private String description;

    /** 分类图标标识 */
    @Column(length = 50)
    private String icon;

    /** 父分类 ID，null 表示顶级分类 */
    @Column(name = "parent_id")
    private Long parentId;

    /** 排序权重，越小越靠前 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** 所属用户 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.sortOrder == null) this.sortOrder = 0;
    }
}
