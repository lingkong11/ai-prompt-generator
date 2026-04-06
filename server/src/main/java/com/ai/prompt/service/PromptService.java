package com.ai.prompt.service;

import com.ai.prompt.entity.PromptTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 提示词生成服务
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
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public PromptService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 生成提示词
     */
    public String generatePrompt(String goal, String type, String style, String language) {
        String systemPrompt = buildSystemPrompt(type);
        String userPrompt = buildUserPrompt(goal, type, style, language);
        
        if (apiKey != null && !apiKey.isEmpty()) {
            return generateWithAI(systemPrompt, userPrompt);
        }
        
        return generateLocalPrompt(goal, type, style, language);
    }
    
    private String buildSystemPrompt(String type) {
        if ("writing".equals(type)) {
            return "你是一个专业的写作助手，擅长创建清晰、精确的提示词来引导AI生成高质量内容。";
        } else if ("coding".equals(type)) {
            return "你是一个编程专家，擅长创建技术精确的代码提示词。";
        } else if ("analysis".equals(type)) {
            return "你是一个数据分析专家，擅长创建分析任务提示词。";
        } else if ("agent".equals(type)) {
            return "你是一个AI工作流专家，擅长创建可执行的Agent任务提示词。";
        } else {
            return "你是一个提示词专家，擅长创建高效的AI提示词。";
        }
    }
    
    private String buildUserPrompt(String goal, String type, String style, String language) {
        String lang = "中文";
        if ("en".equals(language)) lang = "英文";
        
        return String.format("请为以下目标生成一个优化的AI提示词：\n\n目标：%s\n类型：%s\n风格：%s\n语言：%s\n\n要求：\n1. 提示词要详细、具体、可执行\n2. 包含角色定义、任务描述、输出格式要求\n3. 适当加入约束条件和示例\n4. 输出格式应为可直接使用的提示词文本", goal, type, style, lang);
    }
    
    private String generateWithAI(String systemPrompt, String userPrompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(new HashMap<String, String>() {{ put("role", "system"); put("content", systemPrompt); }});
            messages.add(new HashMap<String, String>() {{ put("role", "user"); put("content", userPrompt); }});
            
            requestBody.put("messages", messages);
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                return choices.get(0).get("message").get("content").asText();
            }
            
            return generateLocalPrompt("", "general", "专业", "zh");
            
        } catch (Exception e) {
            log.warn("[时间戳:{}] [阶段:Service] [任务:AI生成] [动作:fallback] - 错误:{}", System.currentTimeMillis(), e.getMessage());
            return generateLocalPrompt("", "general", "专业", "zh");
        }
    }
    
    private String generateLocalPrompt(String goal, String type, String style, String language) {
        String isZh = "zh".equals(language) ? "zh" : "en";
        
        if ("agent".equals(type)) {
            return generateOpenClawPrompt(goal, style, isZh);
        } else if ("writing".equals(type)) {
            return generateWritingPrompt(goal, style, isZh);
        } else if ("coding".equals(type)) {
            return generateCodingPrompt(goal, style, isZh);
        } else if ("analysis".equals(type)) {
            return generateAnalysisPrompt(goal, style, isZh);
        } else {
            return generateGeneralPrompt(goal, style, isZh);
        }
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
                "**Phase 1: Requirements Analysis**\n- Understand user requirements\n- Define functional boundaries\n- Identify core use cases\n\n" +
                "**Phase 2: Technical Design**\n- Technical stack decisions\n- Architecture design\n- Database design\n- API specifications\n\n" +
                "**Phase 3: Code Development**\n- Environment setup\n- Implementation\n- Code review\n\n" +
                "**Phase 4: Functional Testing**\n- Write test cases\n- Execute tests\n- Fix defects\n\n" +
                "**Phase 5: Project Management**\n- Progress tracking\n- Risk assessment\n- Resource coordination\n\n" +
                "**Phase 6: Documentation Output**\n- Operation manual\n- Technical documentation\n\n" +
                "**Phase 7: Project Delivery**\n- Deployment\n- Delivery checklist\n- Acceptance confirmation\n\n" +
                "---\n\n### 📊 Output Requirements\n\nEach phase must output structured logs containing:\n- Timestamp\n- Phase name\n- Task description\n- Action taken\n- Result\n- Risk assessment\n- Next steps\n\n### ⚠️ Constraints\n\n1. Strictly follow nine-phase workflow\n2. Delivery must include complete documentation\n3. Execute tests and fix defects autonomously\n4. Reports must be structured and clear\n5. Code must be runnable and deployable";
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