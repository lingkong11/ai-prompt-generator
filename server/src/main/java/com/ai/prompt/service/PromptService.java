package com.ai.prompt.service;

import com.ai.prompt.common.QuotaExceededException;
import com.ai.prompt.dto.GenerateRequest;
import com.ai.prompt.dto.PromptDTO;
import com.ai.prompt.dto.PromptSaveRequest;
import com.ai.prompt.dto.DtoConverter;
import com.ai.prompt.entity.Category;
import com.ai.prompt.entity.Prompt;
import com.ai.prompt.entity.User;
import com.ai.prompt.repository.CategoryRepository;
import com.ai.prompt.repository.PromptRepository;
import com.ai.prompt.service.ai.AiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 提示词业务服务层
 *
 * <p>封装所有与提示词相关的业务逻辑：生成、CRUD、收藏、统计。
 * Controller 层通过此服务完成业务操作，不直接操作 Repository。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptService {

    private final AiProvider aiProvider;
    private final PromptRepository promptRepository;
    private final CategoryRepository categoryRepository;
    private final SubscriptionService subscriptionService;

    // 线程安全的调用计数器
    private final AtomicInteger totalCalls = new AtomicInteger(0);
    private final AtomicInteger successCalls = new AtomicInteger(0);
    private final AtomicInteger fallbackCalls = new AtomicInteger(0);

    /* ==================== 提示词生成 ==================== */

    /**
     * 根据用户输入生成提示词
     *
     * <p>优先通过 AI 大模型生成，失败时降级到本地模板引擎。
     * 生成前检查用户配额，配额不足时抛出异常。</p>
     *
     * @param userId  用户ID（用于配额校验）
     * @param request 包含 goal / type / style / language 的请求
     * @return 生成的提示词文本
     * @throws QuotaExceededException 配额不足时抛出
     */
    public String generatePrompt(Long userId, GenerateRequest request) {
        // 检查并消耗生成配额
        if (!subscriptionService.consumeGenerateQuota(userId)) {
            throw new QuotaExceededException("今日生成次数已用完，请升级套餐");
        }
        totalCalls.incrementAndGet();
        log.info("生成提示词: goal={}, type={}, aiConfigured={}",
                request.getGoal(), request.getType(), aiProvider.isConfigured());

        String systemPrompt = buildSystemPrompt(request.getType());
        String userPrompt = buildUserPrompt(
                request.getGoal(), request.getType(),
                request.getStyle(), request.getLanguage());

        if (aiProvider.isConfigured()) {
            try {
                String result = aiProvider.generate(systemPrompt, userPrompt, null);
                successCalls.incrementAndGet();
                return result;
            } catch (Exception e) {
                fallbackCalls.incrementAndGet();
                log.warn("AI 生成失败，降级到本地模板: {}", e.getMessage());
            }
        }

        return generateLocalPrompt(request);
    }

    /**
     * 测试提示词（直接调用大模型）
     */
    public Map<String, Object> testPrompt(String prompt) {
        return aiProvider.test(prompt);
    }

    public boolean isApiConfigured() {
        return aiProvider.isConfigured();
    }

    public Map<String, Object> getStats() {
        return Map.of(
                "totalCalls", totalCalls.get(),
                "successCalls", successCalls.get(),
                "fallbackCalls", fallbackCalls.get(),
                "apiConfigured", aiProvider.isConfigured(),
                "provider", aiProvider.providerName()
        );
    }

    /* ==================== 提示词 CRUD ==================== */

    /**
     * 创建提示词并关联到指定用户
     */
    @Transactional
    public PromptDTO createPrompt(PromptSaveRequest request, User user) {
        Prompt prompt = new Prompt();
        copyFromRequest(prompt, request);
        prompt.setUser(user);
        prompt.setIsFavorite(false);
        prompt.setIsTemplate(false);
        prompt.setUseCount(0);

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .ifPresent(prompt::setCategory);
        }

        log.info("创建提示词: userId={}, title={}", user.getId(), request.getTitle());
        return DtoConverter.toPromptDTO(promptRepository.save(prompt));
    }

    /**
     * 分页查询用户的提示词，支持关键词搜索和类型过滤
     */
    @Transactional(readOnly = true)
    public Map<String, Object> listPrompts(Long userId, int page, int size,
                                            String type, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Prompt> prompts;
        if (keyword != null && !keyword.isEmpty()) {
            prompts = promptRepository.searchByUserIdAndKeyword(userId, keyword, pageRequest);
        } else if (type != null && !type.isEmpty()) {
            prompts = promptRepository.findByUserIdAndType(userId, type, pageRequest);
        } else {
            prompts = promptRepository.findByUserId(userId, pageRequest);
        }

        List<PromptDTO> dtoList = prompts.getContent().stream()
                .map(DtoConverter::toPromptDTO)
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("prompts", dtoList);
        data.put("totalElements", prompts.getTotalElements());
        data.put("totalPages", prompts.getTotalPages());
        data.put("currentPage", prompts.getNumber());
        return data;
    }

    /**
     * 查询单条提示词
     */
    @Transactional(readOnly = true)
    public PromptDTO getPrompt(Long promptId, Long userId) {
        Prompt prompt = promptRepository.findById(promptId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElse(null);
        return prompt != null ? DtoConverter.toPromptDTO(prompt) : null;
    }

    /**
     * 更新提示词
     */
    @Transactional
    public PromptDTO updatePrompt(Long promptId, Long userId, PromptSaveRequest request) {
        Prompt prompt = promptRepository.findById(promptId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElse(null);
        if (prompt == null) return null;

        copyFromRequest(prompt, request);
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .ifPresent(prompt::setCategory);
        }

        log.info("更新提示词: promptId={}", promptId);
        return DtoConverter.toPromptDTO(promptRepository.save(prompt));
    }

    /**
     * 删除提示词
     */
    @Transactional
    public boolean deletePrompt(Long promptId, Long userId) {
        return promptRepository.findById(promptId)
                .filter(p -> p.getUser().getId().equals(userId))
                .map(prompt -> {
                    promptRepository.delete(prompt);
                    log.info("删除提示词: promptId={}", promptId);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 切换收藏状态
     */
    @Transactional
    public Boolean toggleFavorite(Long promptId, Long userId) {
        Prompt prompt = promptRepository.findById(promptId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElse(null);
        if (prompt == null) return null;

        prompt.setIsFavorite(!prompt.getIsFavorite());
        promptRepository.save(prompt);
        return prompt.getIsFavorite();
    }

    /**
     * 获取收藏列表
     */
    @Transactional(readOnly = true)
    public List<PromptDTO> getFavorites(Long userId) {
        return promptRepository.findByUserIdAndIsFavoriteTrue(userId).stream()
                .map(DtoConverter::toPromptDTO)
                .collect(Collectors.toList());
    }

    /**
     * 递增使用次数
     */
    @Transactional
    public Integer incrementUseCount(Long promptId, Long userId) {
        Prompt prompt = promptRepository.findById(promptId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElse(null);
        if (prompt == null) return null;

        prompt.setUseCount(prompt.getUseCount() + 1);
        promptRepository.save(prompt);
        return prompt.getUseCount();
    }

    /* ==================== 预置模板 ==================== */

    public List<com.ai.prompt.entity.PromptTemplate> getDefaultTemplates() {
        List<com.ai.prompt.entity.PromptTemplate> templates = new ArrayList<>();

        templates.add(makeTemplate("OpenClaw九阶段工作流", "openclaw",
                "AI Agent标准九阶段开发流程提示词", "openclaw,workflow,九阶段"));
        templates.add(makeTemplate("文章写作助手", "writing",
                "专业文章写作提示词", "写作,文章,文案"));
        templates.add(makeTemplate("代码开发助手", "coding",
                "编程问题解决提示词", "编程,代码,开发"));
        templates.add(makeTemplate("数据分析助手", "analysis",
                "数据分析和报告提示词", "分析,数据,报告"));
        templates.add(makeTemplate("翻译助手", "general",
                "多语言翻译提示词", "翻译,语言,双语"));

        return templates;
    }

    /* ==================== System / User Prompt 构建 ==================== */

    private String buildSystemPrompt(String type) {
        return Map.of(
                "writing", "你是一个专业的写作助手，擅长创建清晰、精确的提示词来引导AI生成高质量内容。请直接输出优化后的提示词，不要添加额外说明。",
                "coding", "你是一个编程专家，擅长创建技术精确的代码提示词。请直接输出优化后的提示词，不要添加额外说明。",
                "analysis", "你是一个数据分析专家，擅长创建分析任务提示词。请直接输出优化后的提示词，不要添加额外说明。",
                "agent", "你是一个AI工作流专家，擅长创建可执行的Agent任务提示词。请直接输出优化后的提示词，不要添加额外说明。"
        ).getOrDefault(type,
                "你是一个提示词专家，擅长创建高效的AI提示词。请直接输出优化后的提示词，不要添加额外说明。");
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

    /* ==================== 本地模板生成（降级方案） ==================== */

    private String generateLocalPrompt(GenerateRequest request) {
        String isZh = "zh".equals(request.getLanguage()) ? "zh" : "en";
        return switch (request.getType()) {
            case "agent" -> generateOpenClawPrompt(request.getGoal(), request.getStyle(), isZh);
            case "writing" -> generateWritingPrompt(request.getGoal(), request.getStyle(), isZh);
            case "coding" -> generateCodingPrompt(request.getGoal(), request.getStyle(), isZh);
            case "analysis" -> generateAnalysisPrompt(request.getGoal(), request.getStyle(), isZh);
            default -> generateGeneralPrompt(request.getGoal(), request.getStyle(), isZh);
        };
    }

    private String generateOpenClawPrompt(String goal, String style, String isZh) {
        String resolvedGoal = (goal == null || goal.isEmpty())
                ? (isZh.equals("zh") ? "请选择一个具体的开发任务" : "Please select a specific development task")
                : goal;

        if ("zh".equals(isZh)) {
            return "## 🤖 OpenClaw AI Agent 工作流提示词\n\n"
                    + "### 任务目标\n" + resolvedGoal + "\n\n---\n\n"
                    + "### 📋 标准九阶段工作流\n\n"
                    + "你是一个自主型AI软件工程Agent，严格遵循以下九阶段闭环流程：\n\n"
                    + "**Phase 1: 需求分析**\n- 理解用户需求\n- 明确功能边界\n- 识别核心用例\n- 输出：需求规格说明书\n\n"
                    + "**Phase 2: 技术设计**\n- 技术选型决策\n- 架构设计\n- 数据库设计\n- API接口规范\n- 输出：技术设计方案\n\n"
                    + "**Phase 3: 代码开发**\n- 环境搭建\n- 编码实现\n- 代码审查\n- 输出：可运行代码\n\n"
                    + "**Phase 4: 功能测试**\n- 编写测试用例\n- 执行测试\n- 修复缺陷\n- 输出：测试报告\n\n"
                    + "**Phase 5: 项目管理**\n- 进度追踪\n- 风险评估\n- 资源协调\n- 输出：项目状态报告\n\n"
                    + "**Phase 6: 文档输出**\n- 操作手册\n- 运维手册\n- 技术方案\n- 输出：完整文档体系\n\n"
                    + "**Phase 7: 项目交付**\n- 部署上线\n- 交付清单\n- 验收确认\n- 输出：交付报告\n\n"
                    + "---\n\n### 📊 输出要求\n\n"
                    + "每个阶段必须输出结构化日志，包含：\n"
                    + "- 时间戳\n- 阶段名称\n- 任务描述\n- 执行动作\n- 执行结果\n- 风险评估\n- 下一步计划\n\n"
                    + "### ⚠️ 约束条件\n\n"
                    + "1. 严格遵循九阶段闭环，不接受跳步\n"
                    + "2. 交付必须包含完整文档体系\n"
                    + "3. 自主执行测试并修复缺陷\n"
                    + "4. 报告必须结构化，字段清晰\n"
                    + "5. 代码必须可运行、可部署";
        } else {
            return "## 🤖 OpenClaw AI Agent Workflow Prompt\n\n"
                    + "### Task Goal\n" + resolvedGoal + "\n\n---\n\n"
                    + "### 📋 Standard Nine-Phase Workflow\n\n"
                    + "You are an autonomous AI software engineering Agent, strictly following this nine-phase closed loop:\n\n"
                    + "**Phase 1: Requirements Analysis** - Understand user requirements, define functional boundaries, identify core use cases\n\n"
                    + "**Phase 2: Technical Design** - Technical stack decisions, architecture design, database design, API specifications\n\n"
                    + "**Phase 3: Code Development** - Environment setup, implementation, code review\n\n"
                    + "**Phase 4: Functional Testing** - Write test cases, execute tests, fix defects\n\n"
                    + "**Phase 5: Project Management** - Progress tracking, risk assessment, resource coordination\n\n"
                    + "**Phase 6: Documentation Output** - Operation manual, technical documentation\n\n"
                    + "**Phase 7: Project Delivery** - Deployment, delivery checklist, acceptance confirmation\n\n"
                    + "---\n\n### 📊 Output Requirements\n\n"
                    + "Each phase must output structured logs containing: Timestamp, Phase name, Task description, Action taken, Result, Risk assessment, Next steps\n\n"
                    + "### ⚠️ Constraints\n\n"
                    + "1. Strictly follow nine-phase workflow\n"
                    + "2. Delivery must include complete documentation\n"
                    + "3. Execute tests and fix defects autonomously\n"
                    + "4. Reports must be structured and clear\n"
                    + "5. Code must be runnable and deployable";
        }
    }

    private String generateWritingPrompt(String goal, String style, String isZh) {
        if ("zh".equals(isZh)) {
            return "## 📝 AI写作助手提示词\n\n**目标**：" + goal + "\n\n"
                    + "### 角色定义\n你是一位专业的文案撰写专家，擅长各类文章创作。\n\n"
                    + "### 任务要求\n1. 理解用户需求，创作符合目标的文章\n"
                    + "2. 根据风格要求调整文风和表达方式\n"
                    + "3. 确保内容逻辑清晰、结构合理\n\n"
                    + "### 输出格式\n- 标题\n- 正文（多段落）\n- 总结\n\n"
                    + "### 风格要求：" + style;
        } else {
            return "## 📝 AI Writing Assistant Prompt\n\n**Goal**: " + goal + "\n\n"
                    + "### Role\nYou are a professional copywriting expert.\n\n"
                    + "### Requirements\n1. Understand user needs\n2. Adjust style accordingly\n3. Ensure clear logic and structure\n\n"
                    + "### Output Format\n- Title\n- Body (multiple paragraphs)\n- Summary\n\n"
                    + "### Style: " + style;
        }
    }

    private String generateCodingPrompt(String goal, String style, String isZh) {
        if ("zh".equals(isZh)) {
            return "## 💻 AI编程助手提示词\n\n**目标**：" + goal + "\n\n"
                    + "### 角色定义\n你是一位资深的全栈开发工程师，精通多种编程语言和框架。\n\n"
                    + "### 任务要求\n1. 提供清晰、可执行的代码解决方案\n"
                    + "2. 包含完整的代码实现和注释\n"
                    + "3. 说明技术选型的理由\n\n"
                    + "### 输出格式\n1. 问题分析\n2. 技术方案\n3. 代码实现\n4. 使用说明\n\n"
                    + "### 代码风格：" + style;
        } else {
            return "## 💻 AI Coding Assistant Prompt\n\n**Goal**: " + goal + "\n\n"
                    + "### Role\nYou are a senior full-stack developer.\n\n"
                    + "### Requirements\n1. Provide clear, executable solutions\n"
                    + "2. Include complete code with comments\n3. Explain technical decisions\n\n"
                    + "### Output Format\n1. Problem analysis\n2. Technical solution\n3. Code implementation\n4. Usage guide\n\n"
                    + "### Code Style: " + style;
        }
    }

    private String generateAnalysisPrompt(String goal, String style, String isZh) {
        if ("zh".equals(isZh)) {
            return "## 📊 AI分析助手提示词\n\n**目标**：" + goal + "\n\n"
                    + "### 角色定义\n你是一位数据分析师，擅长从多角度分析问题。\n\n"
                    + "### 任务要求\n1. 收集相关信息，进行全面分析\n"
                    + "2. 提供数据支持和逻辑推理\n"
                    + "3. 给出可行的建议和解决方案\n\n"
                    + "### 输出格式\n1. 现状分析\n2. 问题识别\n3. 原因分析\n4. 建议方案\n5. 预期效果\n\n"
                    + "### 分析风格：" + style;
        } else {
            return "## 📊 AI Analysis Assistant Prompt\n\n**Goal**: " + goal + "\n\n"
                    + "### Role\nYou are a data analyst.\n\n"
                    + "### Requirements\n1. Collect information for comprehensive analysis\n"
                    + "2. Provide data support and logical reasoning\n"
                    + "3. Give feasible suggestions and solutions\n\n"
                    + "### Output Format\n1. Current status analysis\n2. Problem identification\n"
                    + "3. Root cause analysis\n4. Recommended solutions\n5. Expected outcomes\n\n"
                    + "### Analysis Style: " + style;
        }
    }

    private String generateGeneralPrompt(String goal, String style, String isZh) {
        if ("zh".equals(isZh)) {
            return "## 🎯 AI提示词生成结果\n\n**目标**：" + goal + "\n**风格**：" + style + "\n\n"
                    + "### 优化后的提示词\n\n"
                    + "请根据以上目标，生成一个高质量的AI提示词，包含：\n"
                    + "1. 明确的角色定义\n2. 详细的任务描述\n"
                    + "3. 具体的输出格式要求\n4. 必要的约束条件\n\n"
                    + "---\n*提示词类型：通用 | 语言：中文*";
        } else {
            return "## 🎯 AI Prompt Generation Result\n\n**Goal**: " + goal + "\n**Style**: " + style + "\n\n"
                    + "### Optimized Prompt\n\n"
                    + "Generate a high-quality AI prompt including:\n"
                    + "1. Clear role definition\n2. Detailed task description\n"
                    + "3. Specific output format requirements\n4. Necessary constraints\n\n"
                    + "---\n*Type: General | Language: English*";
        }
    }

    /* ==================== 工具方法 ==================== */

    private void copyFromRequest(Prompt prompt, PromptSaveRequest request) {
        prompt.setTitle(request.getTitle());
        prompt.setContent(request.getContent());
        prompt.setType(request.getType());
        prompt.setStyle(request.getStyle());
        prompt.setLanguage(request.getLanguage());
        prompt.setGoal(request.getGoal());
        prompt.setTags(request.getTags());
    }

    private com.ai.prompt.entity.PromptTemplate makeTemplate(
            String name, String category, String description, String tags) {
        com.ai.prompt.entity.PromptTemplate t = new com.ai.prompt.entity.PromptTemplate();
        t.setName(name);
        t.setCategory(category);
        t.setDescription(description);
        t.setTags(tags);
        return t;
    }
}
