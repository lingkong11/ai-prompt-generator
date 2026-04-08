package com.ai.prompt.dto;

import com.ai.prompt.entity.Prompt;

/**
 * DTO 转换器
 *
 * <p>集中处理 Entity 到 DTO 的映射逻辑，Controller 层不直接序列化 Entity。</p>
 */
public final class DtoConverter {

    private DtoConverter() {
        // 工具类禁止实例化
    }

    /**
     * Prompt Entity → PromptDTO
     */
    public static PromptDTO toPromptDTO(Prompt prompt) {
        PromptDTO dto = new PromptDTO();
        dto.setId(prompt.getId());
        dto.setTitle(prompt.getTitle());
        dto.setContent(prompt.getContent());
        dto.setType(prompt.getType());
        dto.setStyle(prompt.getStyle());
        dto.setLanguage(prompt.getLanguage());
        dto.setGoal(prompt.getGoal());
        dto.setTags(prompt.getTags());
        dto.setIsFavorite(prompt.getIsFavorite());
        dto.setIsTemplate(prompt.getIsTemplate());
        dto.setUseCount(prompt.getUseCount());
        dto.setCreatedAt(prompt.getCreatedAt());
        dto.setUpdatedAt(prompt.getUpdatedAt());

        if (prompt.getCategory() != null) {
            dto.setCategoryId(prompt.getCategory().getId());
            dto.setCategoryName(prompt.getCategory().getName());
        }

        return dto;
    }
}
