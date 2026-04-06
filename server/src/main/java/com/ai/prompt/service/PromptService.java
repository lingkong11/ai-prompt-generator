package com.ai.prompt.service;

import com.ai.prompt.entity.PromptTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 提示词生成服务
 * 
 * 优化版本：
 * 1. 支持真实大模型API调用（DeepSeek/OpenAI）
 * 2. 完善错误处理和日志
 * 3. 支持测试接口真实调用
 */
@Slf4j
@Service
public class PromptService {
    
    @Value("${ai.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${ai.api.key:}")
    private String apiKey;
    
    @Value("${ai.model:deepseek-chat}")
    private String model;
    
    @Value("${ai.api.timeout:30000}")
    private int apiTimeout;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // 统计信息
    private int totalCalls = 0;
    private int successCalls = 0;
    private int fallbackCalls = 0;
    
    public PromptService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 检查API是否已配置
     */
    public boolean isApiConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("${DP_AI_API_KEY:}");
    }
    
    /**
     * 生成提示词（主入口）
     */
    public String generatePrompt(String goal, String type, String style, String language) {
        long startTime = System.currentTimeMillis();
        totalCalls++;
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        log.info("[时间戳:{}] [阶段:Service] [任务:生成提示词] [动作:generate] - 目标:{} 类型:{} API配置:{}", 
            timestamp, goal, type, isApiConfigured());
        
        String systemPrompt = buildSystemPrompt(type);
        String userPrompt = buildUserPrompt(goal, type, style, language);
        
        if (isApiConfigured()) {
            try {
                String result = generateWithAI(systemPrompt, userPrompt);
                successCalls++;
                long duration = System.currentTimeMillis() - startTime;
                log.info("[时间戳:{}] [阶段:Service] [任务:AI生成完成] [动作:success] - 耗时:{}ms", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), duration);
                return result;
            } catch (Exception e) {
                fallbackCalls++;
                log.warn("[时间戳:{}] [阶段:Service] [任务:AI生成失败] [动作:fallback] - 错误:{}", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), e.getMessage());
                return generateLocalPrompt(goal, type, style, language);
            }
        }
        
        log.info("[时间戳:{}] [阶段:Service] [任务:使用本地模板] [动作:local]", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        return generateLocalPrompt(goal, type, style, language);
    }
    
    /**
     * 测试提示词（真实调用大模型）
     */
    public Map<String, Object> testPrompt(String prompt) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        result.put("timestamp", timestamp);
        result.put("prompt", prompt);
        result.put("apiConfigured", isApiConfigured());
        result.put("model", model);
        result.put("apiUrl", apiUrl);
        
        if (!isApiConfigured()) {
            result.put("status", "error");
            result.put("error", "API密钥未配置，请设置环境变量 DP_AI_API_KEY 或在 application.yml 中配置 ai.api.key");
            result.put("suggestion", "示例: export DP_AI_API_KEY=sk-xxxxx 或在 application.yml 中设置 ai.api.key: sk-xxxxx");
            return result;
        }
        
        try {
            // 构建测试请求
            Map<String, Object> requestBody = new LinkedHashMap<>();
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(new LinkedHashMap<String, String>() {{ 
                put("role", "user"); 
                put("content", prompt); 
            }});
            
            requestBody.put("messages", messages);
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("[时间戳:{}] [阶段:Service] [任务:测试提示词] [动作:调用AI] - 模型:{}", timestamp, model);
            
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();
                
                // 提取usage信息
                JsonNode usage = root.get("usage");
                int promptTokens = usage != null && usage.has("prompt_tokens") ? usage.get("prompt_tokens").asInt() : 0;
                int completionTokens = usage != null && usage.has("completion_tokens") ? usage.get("completion_tokens").asInt() : 0;
                int totalTokens = usage != null && usage.has("total_tokens") ? usage.get("total_tokens").asInt() : 0;
                
                long duration = System.currentTimeMillis() - startTime;
                
                result.put("status", "success");
                result.put("result", content);
                result.put("duration", duration + "ms");
                result.put("usage", Map.of(
                    "prompt_tokens", promptTokens,
                    "completion_tokens", completionTokens,
                    "total_tokens", totalTokens
                ));
                
                log.info("[时间戳:{}] [阶段:Service] [任务:测试完成] [动作:success] - 耗时:{}ms Tokens:{}", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), duration, totalTokens);
            } else {
                result.put("status", "error");
                result.put("error", "API返回格式异常");
                result.put("rawResponse", response.getBody());
            }
            
        } catch (RestClientException e) {
            result.put("status", "error");
            result.put("error", "API调用失败: " + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            log.error("[时间戳:{}] [阶段:Service] [任务:测试失败] [动作:error] - {}", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), e.getMessage());
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", "处理异常: " + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            log.error("[时间戳:{}] [阶段:Service] [任务:测试失败] [动作:error] - {}", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取服务统计信息
     */
    public Map<String, Object> getStats() {
        return Map.of(
            "totalCalls", totalCalls,
            "successCalls", successCalls,
            "fallbackCalls", fallbackCalls,
            "apiConfigured", isApiConfigured(),
            "model", model,
            "apiUrl", apiUrl
        );
    }
    
    // ==================== 私有方法 ====================
    
    private String buildSystemPrompt(String type) {
        Map<String, String> systemPrompts = Map.of(
            "writing", "你是一个专业的写作助手，擅长创建清晰、精确的提示词来引导AI生成高质量内容。请直接输出优化后的提示词，不要添加额外说明。",
            "coding", "你是一个编程专家，擅长创建技术精确的代码提示词。请直接输出优化后的提示词，不要添加额外说明。",
            "analysis", "你是一个数据分析专家，擅长创建分析任务提示词。请直接输出优化后的提示词，不要添加额外说明。",
            "agent", "你是一个AI工作流专家，擅长创建可执行的Agent任务提示词。请直接输出优化后的提示词，不要添加额外说明。"
        );
        return systemPrompts.getOrDefault(type, "你是一个提示词专家，擅长创建高效的AI提示词。请直接输出优化后的提示词，不要添加额外说明。");
    }
    
    private String buildUserPrompt(String goal, String type, String style, String language) {
        String lang = "en".equals(language) ? "英文" : "中文";
        String typeDesc = Map.of(
            "writing", "写作",
            "coding", "编程",
            "analysis", "分析",
            "agent", "AI Agent工作流"
        ).getOrDefault(type, "通用");
        
        return String.format("""
            请为以下目标生成一个优化的AI提示词：
            
            【目标】%s
            【类型】%s
            【风格】%s
            【输出语言】%s
            
            【要求】
            1. 提示词要详细、具体、可执行
            2. 包含角色定义、任务描述、输出格式要求
            3. 适当加入约束条件和示例
            4. 输出格式应为可直接使用的提示词文本
            5. 不要添加"以下是生成的提示词"等说明，直接输出提示词内容
            """, goal, typeDesc, style, lang);
    }
    
    private String generateWithAI(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(new LinkedHashMap<String, String>() {{ put("role", "system"); put("content", systemPrompt); }});
        messages.add(new LinkedHashMap<String, String>() {{ put("role", "user"); put("content", userPrompt); }});
        
        requestBody.put("messages", messages);
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                return choices.get(0).get("message").get("content").asText();
            }
        } catch (Exception e) {
            log.error("解析API响应失败: {}", e.getMessage());
        }
        
        throw new RuntimeException("API返回格式异常：无法解析choices");
    }
    
    private String generateLocalPrompt(String goal, String type, String style, String language) {
        String isZh = "zh".equals(language) ? "zh" : "en";
        
        return switch (type) {
            case "agent" -> generateOpenClawPrompt(goal, style, isZh);
            case "writing" -> generateWritingPrompt(goal, style, isZh);
            case "coding" -> generateCodingPrompt(goal, style, isZh);
            case "analysis" -> generateAnalysisPrompt(goal, style, isZh);
            default -> generateGeneralPrompt(goal, style, isZh);
        };
    }
    
    private String generateOpenClawPrompt(String goal, String style, String isZh) {
        String resolvedGoal = (goal == null || goal.isEmpty()) ? 
            (isZh.equals("zh") ? "请选择一个具体的开发任务" : "Please select a specific development task") : goal;
        
        if ("zh".equals(isZh)) {
            return "## 🤖 OpenClaw AI Agent 工作流提示词\n\n### 任务目标\n" + resolvedGoal + "\n\n---\n\n### 📋 标准九阶段工作流\n\n" +
                "你是一个自主型AI软件工程Agent，严格遵循以下九阶段闭环流程：\n\n" +
                "**Phase 1: 需求分析**\n- 理解用户需求\n- 明确功能边界\n- 识别核心用例\n- 输出：需求规格说明书\n\n" +
                "**Phase 2: 技术设计**\n- 技术选型决策\n- 架构设计\n- 数据库设计\n- API接口规范\n- 输出：技术设计方案\n\n" +
                "**Phase 3: 代码开发**\n- 环境搭建\n- 编码实现\n- 代码审查\n- 输出：可运行代码\n\n" +
                "**Phase 4: 功能测试**\n- 编写测试用例\n- 执行测试\n- 修复缺陷\n- 输出：测试报告\n\n" +
                "**Phase 5: 项目管理**\n- 进度追踪\n- 风险评估\n- 资源协调\n- 输出：项目状态报告\n\n" +
                "**Phase 6: 文档输出**\n- 操作手册\n- 运维手册\n- 技术方案\n- 输出：完整文档体系\n\n" +
                "**Phase 7: 项目交付**\n- 部署上线\n- 交付清单\n- 验收确认\n- 输出：交付报告\n\n" +
                "---\n\n### 📊 输出要求\n\n每个阶段必须输出结构化日志，包含：\n- 时间戳\n- 阶段名称\n- 任务描述\n- 执行动作\n- 执行结果\n- 风险评估\n- 下一步计划\n\n### ⚠️ 约束条件\n\n1. 严格遵循九阶段闭环，不接受跳步\n2. 交付必须包含完整文档体系\n3. 自主执行测试并修复缺陷\n4. 报告必须结构化，字段清晰\n5. 代码必须可运行、可部署\n\n### 💡 使用说明\n\n此提示词适用于：\n- 完整项目开发\n- 功能模块开发\n- 代码审查与重构\n- 技术方案设计\n- 文档生成与整理";
        } else {
            return "## 🤖 OpenClaw AI Agent Workflow Prompt\n\n### Task Goal\n" + resolvedGoal + "\n\n---\n\n### 📋 Standard Nine-Phase Workflow\n\n" +
                "You are an autonomous AI software engineering Agent, strictly following this nine-phase closed loop:\n\n" +
                "**Phase 1: Requirements Analysis** - Understand user requirements, define functional boundaries, identify core use cases\n\n" +
                "**Phase 2: Technical Design** - Technical stack decisions, architecture design, database design, API specifications\n\n" +
                "**Phase 3: Code Development** - Environment setup, implementation, code review\n\n" +
                "**Phase 4: Functional Testing** - Write test cases, execute tests, fix defects\n\n" +
                "**Phase 5: Project Management** - Progress tracking, risk assessment, resource coordination\n\n" +
                "**Phase 6: Documentation Output** - Operation manual, technical documentation\n\n" +
                "**Phase 7: Project Delivery** - Deployment, delivery checklist, acceptance confirmation\n\n" +
                "---\n\n### 📊 Output Requirements\n\nEach phase must output structured logs containing: Timestamp, Phase name, Task description, Action taken, Result, Risk assessment, Next steps\n\n### ⚠️ Constraints\n\n1. Strictly follow nine-phase workflow\n2. Delivery must include complete documentation\n3. Execute tests and fix defects autonomously\n4. Reports must be structured and clear\n5. Code must be runnable and deployable";
        }
    }
    
    private String generateWritingPrompt(String goal, String style, String isZh) {
        if ("zh".equals(isZh)) {
            return "## 📝 AI写作助手提示词\n\n**目标**：" + goal + "\n\n### 角色定义\n你是一位专业的文案撰写专家，擅长各类文章创作。\n\n### 任务要求\n1. 理解用户需求，创作符合目标的文章\n2. 根据风格要求调整文风和表达方式\n3. 确保内容逻辑清晰、结构合理\n\n### 输出格式\n- 标题\n- 正文（多段落）\n- 总结\n\n### 风格要求：" + style;
        } else {
            return "## 📝 AI Writing Assistant Prompt\n\n**Goal**: " + goal + "\n\n### Role\nYou are a professional copywriting expert.\n\n### Requirements\n1. Understand user needs\n2. Adjust style accordingly\n3. Ensure clear logic and structure\n\n### Output Format\n- Title\n- Body (multiple paragraphs)\n- Summary\n\n### Style: " + style;
        }
    }
    
    private String generateCodingPrompt(String goal, String style, String isZh) {
        if ("zh".equals(isZh)) {
            return "## 💻 AI编程助手提示词\n\n**目标**：" + goal + "\n\n### 角色定义\n你是一位资深的全栈开发工程师，精通多种编程语言和框架。\n\n### 任务要求\n1. 提供清晰、可执行的代码解决方案\n2. 包含完整的代码实现和注释\n3. 说明技术选型的理由\n\n### 输出格式\n1. 问题分析\n2. 技术方案\n3. 代码实现\n4. 使用说明\n\n### 代码风格：" + style;
        } else {
            return "## 💻 AI Coding Assistant Prompt\n\n**Goal**: " + goal + "\n\n### Role\nYou are a senior full-stack developer.\n\n### Requirements\n1. Provide clear, executable solutions\n2. Include complete code with comments\n3. Explain technical decisions\n\n### Output Format\n1. Problem analysis\n2. Technical solution\n3. Code implementation\n4. Usage guide\n\n### Code Style: " + style;
        }
    }
    
    private String generateAnalysisPrompt(String goal, String style, String isZh) {
        if ("zh".equals(isZh)) {
            return "## 📊 AI分析助手提示词\n\n**目标**：" + goal + "\n\n### 角色定义\n你是一位数据分析师，擅长从多角度分析问题。\n\n### 任务要求\n1. 收集相关信息，进行全面分析\n2. 提供数据支持和逻辑推理\n3. 给出可行的建议和解决方案\n\n### 输出格式\n1. 现状分析\n2. 问题识别\n3. 原因分析\n4. 建议方案\n5. 预期效果\n\n### 分析风格：" + style;
        } else {
            return "## 📊 AI Analysis Assistant Prompt\n\n**Goal**: " + goal + "\n\n### Role\nYou are a data analyst.\n\n### Requirements\n1. Collect information for comprehensive analysis\n2. Provide data support and logical reasoning\n3. Give feasible suggestions and solutions\n\n### Output Format\n1. Current status analysis\n2. Problem identification\n3. Root cause analysis\n4. Recommended solutions\n5. Expected outcomes\n\n### Analysis Style: " + style;
        }
    }
    
    private String generateGeneralPrompt(String goal, String style, String isZh) {
        if ("zh".equals(isZh)) {
            return "## 🎯 AI提示词生成结果\n\n**目标**：" + goal + "\n**风格**：" + style + "\n\n### 优化后的提示词\n\n请根据以上目标，生成一个高质量的AI提示词，包含：\n1. 明确的角色定义\n2. 详细的任务描述\n3. 具体的输出格式要求\n4. 必要的约束条件\n\n---\n*提示词类型：通用 | 语言：中文*";
        } else {
            return "## 🎯 AI Prompt Generation Result\n\n**Goal**: " + goal + "\n**Style**: " + style + "\n\n### Optimized Prompt\n\nGenerate a high-quality AI prompt including:\n1. Clear role definition\n2. Detailed task description\n3. Specific output format requirements\n4. Necessary constraints\n\n---\n*Type: General | Language: English*";
        }
    }
    
    /**
     * 获取预置模板列表
     */
    public List<PromptTemplate> getDefaultTemplates() {
        List<PromptTemplate> templates = new ArrayList<>();
        
        PromptTemplate t1 = new PromptTemplate();
        t1.setName("OpenClaw九阶段工作流");
        t1.setCategory("openclaw");
        t1.setDescription("AI Agent标准九阶段开发流程提示词");
        t1.setTags("openclaw,workflow,九阶段");
        templates.add(t1);
        
        PromptTemplate t2 = new PromptTemplate();
        t2.setName("文章写作助手");
        t2.setCategory("writing");
        t2.setDescription("专业文章写作提示词");
        t2.setTags("写作,文章,文案");
        templates.add(t2);
        
        PromptTemplate t3 = new PromptTemplate();
        t3.setName("代码开发助手");
        t3.setCategory("coding");
        t3.setDescription("编程问题解决提示词");
        t3.setTags("编程,代码,开发");
        templates.add(t3);
        
        PromptTemplate t4 = new PromptTemplate();
        t4.setName("数据分析助手");
        t4.setCategory("analysis");
        t4.setDescription("数据分析和报告提示词");
        t4.setTags("分析,数据,报告");
        templates.add(t4);
        
        PromptTemplate t5 = new PromptTemplate();
        t5.setName("翻译助手");
        t5.setCategory("general");
        t5.setDescription("多语言翻译提示词");
        t5.setTags("翻译,语言,双语");
        templates.add(t5);
        
        return templates;
    }
}
