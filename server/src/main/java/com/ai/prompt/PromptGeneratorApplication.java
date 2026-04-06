package com.ai.prompt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI Prompt Generator Application
 * 
 * 功能：
 * - 提示词生成
 * - 模板管理
 * - OpenClaw专用提示词
 * - 收藏管理
 */
@SpringBootApplication
public class PromptGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromptGeneratorApplication.class, args);
    }
}