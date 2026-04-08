package com.ai.prompt.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词模板（非持久化）
 *
 * <p>用于前端展示的预置模板数据，不映射数据库表。
 * 模板内容由 {@link com.ai.prompt.service.PromptService#getDefaultTemplates()} 构建。</p>
 */
@Data
public class PromptTemplate {

    /** 模板唯一标识（UUID） */
    private String id;

    /** 模板名称 */
    private String name;

    /** 模板分类：openclaw / writing / coding / analysis / general */
    private String category;

    /** 模板简述 */
    private String description;

    /** 模板正文内容 */
    private String content;

    /** 逗号分隔的标签 */
    private String tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PromptTemplate() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
