package com.ai.prompt.controller;

import com.ai.prompt.common.ApiResult;
import com.ai.prompt.dto.GenerateRequest;
import com.ai.prompt.dto.TestPromptRequest;
import com.ai.prompt.service.PromptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示词生成接口
 *
 * <p>提供 Prompt 生成、测试、模板查询、批量生成、统计等能力。
 * 生成和测试接口无需登录（公开体验），其余查询类接口同理。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/prompt")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    /**
     * 服务健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResult<Map<String, Object>>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", "AI Prompt Generator");
        data.put("version", "1.0.0");
        data.put("apiConfigured", promptService.isApiConfigured());
        return ResponseEntity.ok(ApiResult.success(data));
    }

    /**
     * 生成提示词（单条）
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResult<Map<String, Object>>> generate(
            @Valid @RequestBody GenerateRequest request) {

        long start = System.currentTimeMillis();
        log.info("生成提示词: goal={}, type={}", request.getGoal(), request.getType());

        try {
            String prompt = promptService.generatePrompt(request);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("prompt", prompt);
            data.put("goal", request.getGoal());
            data.put("type", request.getType());
            data.put("style", request.getStyle());
            data.put("language", request.getLanguage());
            data.put("apiConfigured", promptService.isApiConfigured());
            data.put("duration", (System.currentTimeMillis() - start) + "ms");

            return ResponseEntity.ok(ApiResult.success(data));
        } catch (Exception e) {
            log.error("生成提示词失败: goal={}, error={}", request.getGoal(), e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResult.error(500, "生成失败: " + e.getMessage()));
        }
    }

    /**
     * 获取预置模板列表
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResult<Map<String, Object>>> getTemplates() {
        var templates = promptService.getDefaultTemplates();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("templates", templates);
        data.put("count", templates.size());
        return ResponseEntity.ok(ApiResult.success(data));
    }

    /**
     * 测试提示词（真实调用大模型）
     */
    @PostMapping("/test")
    public ResponseEntity<ApiResult<Map<String, Object>>> testPrompt(
            @Valid @RequestBody TestPromptRequest request) {

        log.info("测试提示词: length={}", request.getPrompt().length());
        Map<String, Object> result = promptService.testPrompt(request.getPrompt());

        if ("success".equals(result.get("status"))) {
            return ResponseEntity.ok(ApiResult.success(result));
        } else {
            return ResponseEntity.status(500)
                    .body(ApiResult.error(500, (String) result.get("error")));
        }
    }

    /**
     * 运行统计
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResult<Map<String, Object>>> getStats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stats", promptService.getStats());
        return ResponseEntity.ok(ApiResult.success(data));
    }

    /**
     * 批量生成提示词
     *
     * <p>对每个 goal 独立生成，单个失败不影响其余。</p>
     */
    @PostMapping("/batch")
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResult<Map<String, Object>>> batchGenerate(
            @RequestBody Map<String, Object> request) {

        long start = System.currentTimeMillis();
        List<String> goals = (List<String>) request.getOrDefault("goals", new ArrayList<>());
        String type = (String) request.getOrDefault("type", "general");
        String style = (String) request.getOrDefault("style", "专业");
        String language = (String) request.getOrDefault("language", "zh");

        log.info("批量生成: count={}, type={}", goals.size(), type);

        List<Map<String, Object>> results = new ArrayList<>();
        for (String goal : goals) {
            try {
                GenerateRequest genReq = new GenerateRequest();
                genReq.setGoal(goal);
                genReq.setType(type);
                genReq.setStyle(style);
                genReq.setLanguage(language);

                String prompt = promptService.generatePrompt(genReq);
                results.add(Map.of("goal", goal, "prompt", prompt, "status", "success"));
            } catch (Exception e) {
                results.add(Map.of("goal", goal, "error", e.getMessage(), "status", "error"));
            }
        }

        long successCount = results.stream()
                .filter(r -> "success".equals(r.get("status"))).count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", goals.size());
        data.put("success", successCount);
        data.put("failed", results.size() - successCount);
        data.put("results", results);
        data.put("duration", (System.currentTimeMillis() - start) + "ms");

        return ResponseEntity.ok(ApiResult.success(data));
    }
}
