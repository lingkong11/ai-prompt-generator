package com.ai.prompt.service.ai;

import java.util.Map;

/**
 * AI 大模型提供者抽象
 *
 * <p>采用策略模式，每个 AI 服务商（DeepSeek、OpenAI、Claude 等）实现此接口。
 * PromptService 通过注入 AiProvider 完成模型调用，新增提供商只需添加实现类，
 * 无需改动业务逻辑。</p>
 */
public interface AiProvider {

    /**
     * 调用大模型生成文本
     *
     * @param systemPrompt 系统提示词，定义 AI 角色和行为约束
     * @param userPrompt   用户提示词，携带具体任务指令
     * @param config       额外参数（temperature、maxTokens 等），可为空
     * @return 大模型返回的文本内容
     * @throws RuntimeException API 调用失败或响应解析异常
     */
    String generate(String systemPrompt, String userPrompt, Map<String, Object> config);

    /**
     * 单轮测试调用（仅 user 消息），用于连接检测和前端测试面板
     *
     * @param prompt 用户输入的测试文本
     * @return 包含 result、usage、duration 的 Map
     */
    Map<String, Object> test(String prompt);

    /**
     * 当前提供商是否已正确配置（API Key 等必要参数非空）
     */
    boolean isConfigured();

    /**
     * 提供商标识，如 "deepseek"、"openai"
     */
    String providerName();
}
