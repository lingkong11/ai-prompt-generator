package com.ai.prompt.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词模板实体
 */
@Data
public class PromptTemplate {
    private String id;
    private String name;
    private String category;
    private String description;
    private String content;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public PromptTemplate() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}