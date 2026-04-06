package com.ai.prompt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词实体
 */
@Data
@Entity
@Table(name = "prompts")
public class Prompt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 50)
    private String type;
    
    @Column(length = 50)
    private String style;
    
    @Column(length = 10)
    private String language;
    
    @Column(length = 100)
    private String goal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(length = 200)
    private String tags;
    
    @Column(name = "is_favorite")
    private Boolean isFavorite = false;
    
    @Column(name = "is_template")
    private Boolean isTemplate = false;
    
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
