package com.ai.prompt.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek API 适配实现
 *
 * <p>兼容 OpenAI Chat Completions 协议（/v1/chat/completions），
 * 因此也可用于任何兼容 OpenAI 格式的自建或第三方服务。</p>
 *
 * <p>当 {@code ai.provider=deepseek} 或未指定 provider 时激活此 Bean。
 * 新增提供商（如 OpenAI、Claude）只需创建另一个实现类并切换 @ConditionalOnProperty 即可。</p>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "deepseek", matchIfMissing = true)
public class DeepSeekProvider implements AiProvider {

    @Value("${ai.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generate(String systemPrompt, String userPrompt, Map<String, Object> config) {
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        Map<String, Object> body = buildRequestBody(messages, config);

        String raw = callApi(body);
        return extractContent(raw);
    }

    @Override
    public Map<String, Object> test(String prompt) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("model", model);
        result.put("apiUrl", apiUrl);
        result.put("apiConfigured", isConfigured());

        if (!isConfigured()) {
            result.put("status", "error");
            result.put("error", "API 密钥未配置，请设置 ai.api.key");
            return result;
        }

        try {
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "user", "content", prompt)
            );
            Map<String, Object> body = buildRequestBody(messages, Map.of("max_tokens", 2000));
            String raw = callApi(body);
            String content = extractContent(raw);

            JsonNode root = objectMapper.readTree(raw);
            JsonNode usage = root.get("usage");
            int promptTokens = usage != null && usage.has("prompt_tokens") ? usage.get("prompt_tokens").asInt() : 0;
            int completionTokens = usage != null && usage.has("completion_tokens") ? usage.get("completion_tokens").asInt() : 0;

            long duration = System.currentTimeMillis() - start;
            result.put("status", "success");
            result.put("result", content);
            result.put("duration", duration + "ms");
            result.put("usage", Map.of(
                    "prompt_tokens", promptTokens,
                    "completion_tokens", completionTokens,
                    "total_tokens", promptTokens + completionTokens
            ));
        } catch (RestClientException e) {
            log.error("DeepSeek API 调用失败: {}", e.getMessage());
            result.put("status", "error");
            result.put("error", "API 调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("DeepSeek 响应解析失败: {}", e.getMessage());
            result.put("status", "error");
            result.put("error", "处理异常: " + e.getMessage());
        }

        return result;
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("${");
    }

    @Override
    public String providerName() {
        return "deepseek";
    }

    /* ========== 内部方法 ========== */

    private Map<String, Object> buildRequestBody(List<Map<String, String>> messages, Map<String, Object> config) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messages", messages);
        body.put("model", model);
        body.put("temperature", config != null && config.containsKey("temperature")
                ? config.get("temperature") : 0.7);
        body.put("max_tokens", config != null && config.containsKey("max_tokens")
                ? config.get("max_tokens") : 2000);
        return body;
    }

    /**
     * 发送请求并返回原始 JSON 字符串
     */
    private String callApi(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(apiUrl, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("API 返回非成功状态: " + resp.getStatusCode());
        }
        return resp.getBody();
    }

    /**
     * 从 OpenAI 格式响应中提取第一条 choice 的 content
     */
    private String extractContent(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).get("message").get("content").asText();
            }
        } catch (Exception e) {
            log.error("解析 DeepSeek 响应失败: {}", e.getMessage());
        }
        throw new RuntimeException("API 返回格式异常：无法解析 choices");
    }
}
