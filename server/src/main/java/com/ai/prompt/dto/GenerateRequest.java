package com.ai.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提示词生成请求
 */
@Data
public class GenerateRequest {

    /** 生成目标描述 */
    @NotBlank(message = "目标描述不能为空")
    private String goal;

    /** 提示词类型：general / writing / coding / analysis / agent */
    private String type = "general";

    /** 期望的文风 */
    private String style = "专业";

    /** 输出语言：zh / en */
    private String language = "zh";
}
