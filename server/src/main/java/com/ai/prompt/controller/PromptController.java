package com.ai.prompt.controller;

import com.ai.prompt.entity.PromptTemplate;
import com.ai.prompt.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 提示词控制器
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
     * 生成提示词
     * POST /api/prompt/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generate(@RequestBody Map<String, String> request) {
        String goal = request.get("goal");
        String type = request.getOrDefault("type", "general");
        String style = request.getOrDefault("style", "专业");
        String language = request.getOrDefault("language", "zh");
        
        log.info("[时间戳:{}] [阶段:Controller] [任务:生成提示词] [动作:generate] - 目标:{} 类型:{}", 
            System.currentTimeMillis(), goal, type);
        
        String prompt = promptService.generatePrompt(goal, type, style, language);
        
        return ResponseEntity.ok(Map.of(
            "prompt", prompt,
            "goal", goal,
            "type", type
        ));
    }
    
    /**
     * 获取预置模板
     * GET /api/prompt/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<List<PromptTemplate>> getTemplates() {
        return ResponseEntity.ok(promptService.getDefaultTemplates());
    }
    
    /**
     * 测试提示词
     * POST /api/prompt/test
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testPrompt(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        
        // TODO: 集成AI服务测试
        
        return ResponseEntity.ok(Map.of(
            "result", "提示词测试功能需要配置AI API密钥",
            "prompt", prompt
        ));
    }
}