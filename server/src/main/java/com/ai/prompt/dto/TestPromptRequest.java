package com.ai.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 测试提示词请求
 */
@Data
public class TestPromptRequest {

    @NotBlank(message = "prompt 不能为空")
    private String prompt;
}
