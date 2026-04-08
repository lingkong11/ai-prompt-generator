package com.ai.prompt.controller;

import com.ai.prompt.common.ApiResult;
import com.ai.prompt.dto.PromptDTO;
import com.ai.prompt.dto.PromptSaveRequest;
import com.ai.prompt.entity.User;
import com.ai.prompt.service.PromptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示词 CRUD 管理
 *
 * <p>提供已保存提示词的增删改查、收藏、使用计数等操作。
 * 所有接口要求登录，数据按用户隔离。返回统一使用 PromptDTO。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptManageController {

    private final PromptService promptService;

    /**
     * 创建提示词
     */
    @PostMapping
    public ResponseEntity<ApiResult<Map<String, Object>>> createPrompt(
            Authentication authentication,
            @Valid @RequestBody PromptSaveRequest request) {

        User user = (User) authentication.getPrincipal();
        PromptDTO dto = promptService.createPrompt(request, user);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("prompt", dto);
        return ResponseEntity.ok(ApiResult.success(data));
    }

    /**
     * 分页查询当前用户的提示词
     */
    @GetMapping
    public ResponseEntity<ApiResult<Map<String, Object>>> getPrompts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {

        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResult.success(
                promptService.listPrompts(user.getId(), page, size, type, keyword)));
    }

    /**
     * 查询单条提示词
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPrompt(Authentication authentication,
                                       @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        PromptDTO dto = promptService.getPrompt(id, user.getId());

        if (dto == null) return ResponseEntity.notFound().build();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("prompt", dto);
        return ResponseEntity.ok(ApiResult.success(data));
    }

    /**
     * 更新提示词
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePrompt(Authentication authentication,
                                          @PathVariable Long id,
                                          @Valid @RequestBody PromptSaveRequest request) {
        User user = (User) authentication.getPrincipal();
        PromptDTO dto = promptService.updatePrompt(id, user.getId(), request);

        if (dto == null) return ResponseEntity.notFound().build();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("prompt", dto);
        return ResponseEntity.ok(ApiResult.success(data));
    }

    /**
     * 删除提示词
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePrompt(Authentication authentication,
                                          @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        boolean deleted = promptService.deletePrompt(id, user.getId());
        return deleted
                ? ResponseEntity.ok(ApiResult.success())
                : ResponseEntity.notFound().build();
    }

    /**
     * 切换收藏状态
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(Authentication authentication,
                                            @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        Boolean isFavorite = promptService.toggleFavorite(id, user.getId());

        if (isFavorite == null) return ResponseEntity.notFound().build();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("isFavorite", isFavorite);
        return ResponseEntity.ok(ApiResult.success(data));
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/favorites")
    public ResponseEntity<ApiResult<Map<String, Object>>> getFavorites(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        List<PromptDTO> list = promptService.getFavorites(user.getId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("prompts", list);
        return ResponseEntity.ok(ApiResult.success(data));
    }

    /**
     * 递增使用次数
     */
    @PostMapping("/{id}/use")
    public ResponseEntity<?> incrementUseCount(Authentication authentication,
                                               @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        Integer count = promptService.incrementUseCount(id, user.getId());

        if (count == null) return ResponseEntity.notFound().build();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("useCount", count);
        return ResponseEntity.ok(ApiResult.success(data));
    }
}
