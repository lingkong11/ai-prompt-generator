package com.ai.prompt.controller;

import com.ai.prompt.entity.PromptTemplate;
import com.ai.prompt.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 提示词控制器
 * 
 * 优化版本：
 * 1. 完善日志输出
 * 2. 增加统计接口
 * 3. 增加健康检查接口
 */
@Slf4j
@RestController
@RequestMapping("/api/prompt")
public class PromptController {
    
    private final PromptService promptService;
    
    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }
    
    /**
     * 健康检查
     * GET /api/prompt/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        health.put("service", "AI Prompt Generator");
        health.put("version", "1.0.0");
        health.put("apiConfigured", promptService.isApiConfigured());
        return ResponseEntity.ok(health);
    }
    
    /**
     * 生成提示词
     * POST /api/prompt/generate
     * 
     * 请求体示例：
     * {
     *   "goal": "写一篇关于AI的文章",
     *   "type": "writing",
     *   "style": "专业",
     *   "language": "zh"
     * }
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody Map<String, String> request) {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        
        String goal = request.getOrDefault("goal", "");
        String type = request.getOrDefault("type", "general");
        String style = request.getOrDefault("style", "专业");
        String language = request.getOrDefault("language", "zh");
        
        log.info("[时间戳:{}] [阶段:Controller] [任务:生成提示词] [动作:generate] - 目标:{} 类型:{}", 
            timestamp, goal, type);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", timestamp);
        response.put("goal", goal);
        response.put("type", type);
        response.put("style", style);
        response.put("language", language);
        response.put("apiConfigured", promptService.isApiConfigured());
        
        try {
            String prompt = promptService.generatePrompt(goal, type, style, language);
            response.put("prompt", prompt);
            response.put("status", "success");
            response.put("duration", (System.currentTimeMillis() - startTime) + "ms");
            
            log.info("[时间戳:{}] [阶段:Controller] [任务:生成完成] [动作:success] - 耗时:{}ms", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), 
                System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("duration", (System.currentTimeMillis() - startTime) + "ms");
            
            log.error("[时间戳:{}] [阶段:Controller] [任务:生成失败] [动作:error] - {}", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取预置模板
     * GET /api/prompt/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getTemplates() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("templates", promptService.getDefaultTemplates());
        response.put("count", promptService.getDefaultTemplates().size());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 测试提示词（真实调用大模型）
     * POST /api/prompt/test
     * 
     * 请求体示例：
     * {
     *   "prompt": "你是一个专业的写作助手，请帮我写一篇关于AI的文章..."
     * }
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testPrompt(@RequestBody Map<String, String> request) {
        String prompt = request.getOrDefault("prompt", "");
        
        log.info("[时间戳:{}] [阶段:Controller] [任务:测试提示词] [动作:test] - 提示词长度:{}", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), prompt.length());
        
        if (prompt.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "error");
            error.put("error", "prompt参数不能为空");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> result = promptService.testPrompt(prompt);
        
        if ("success".equals(result.get("status"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取服务统计信息
     * GET /api/prompt/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("stats", promptService.getStats());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批量生成提示词
     * POST /api/prompt/batch
     * 
     * 请求体示例：
     * {
     *   "goals": ["写一篇AI文章", "写一个登录功能", "分析销售数据"],
     *   "type": "general",
     *   "style": "专业",
     *   "language": "zh"
     * }
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchGenerate(@RequestBody Map<String, Object> request) {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        
        @SuppressWarnings("unchecked")
        List<String> goals = (List<String>) request.getOrDefault("goals", new ArrayList<>());
        String type = (String) request.getOrDefault("type", "general");
        String style = (String) request.getOrDefault("style", "专业");
        String language = (String) request.getOrDefault("language", "zh");
        
        log.info("[时间戳:{}] [阶段:Controller] [任务:批量生成] [动作:batch] - 数量:{}", timestamp, goals.size());
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (String goal : goals) {
            try {
                String prompt = promptService.generatePrompt(goal, type, style, language);
                results.add(Map.of(
                    "goal", goal,
                    "prompt", prompt,
                    "status", "success"
                ));
            } catch (Exception e) {
                results.add(Map.of(
                    "goal", goal,
                    "error", e.getMessage(),
                    "status", "error"
                ));
            }
        }
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", timestamp);
        response.put("total", goals.size());
        response.put("success", results.stream().filter(r -> "success".equals(r.get("status"))).count());
        response.put("failed", results.stream().filter(r -> "error".equals(r.get("status"))).count());
        response.put("results", results);
        response.put("duration", (System.currentTimeMillis() - startTime) + "ms");
        
        return ResponseEntity.ok(response);
    }
}
