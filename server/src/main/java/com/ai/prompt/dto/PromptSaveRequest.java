package com.ai.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提示词保存/更新请求
 */
@Data
public class PromptSaveRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;
    private String type;
    private String style;
    private String language;
    private String goal;
    private String tags;
    private Long categoryId;
}
