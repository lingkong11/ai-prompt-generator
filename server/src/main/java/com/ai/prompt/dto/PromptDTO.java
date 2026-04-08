package com.ai.prompt.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词数据传输对象
 *
 * <p>Controller 层统一使用此 DTO 返回提示词数据，避免直接序列化 Entity
 * 导致的懒加载异常和敏感字段泄漏。</p>
 */
@Data
public class PromptDTO {

    private Long id;
    private String title;
    private String content;
    private String type;
    private String style;
    private String language;
    private String goal;
    private String tags;
    private Boolean isFavorite;
    private Boolean isTemplate;
    private Integer useCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 关联分类的 ID */
    private Long categoryId;
    /** 关联分类的名称 */
    private String categoryName;
}
