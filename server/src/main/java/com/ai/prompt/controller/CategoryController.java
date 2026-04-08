package com.ai.prompt.controller;

import com.ai.prompt.common.ApiResult;
import com.ai.prompt.entity.Category;
import com.ai.prompt.entity.User;
import com.ai.prompt.repository.CategoryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分类管理
 *
 * <p>提示词的分类 CRUD，数据按用户隔离，查询结果按 sortOrder 升序排列。
 * 分类支持父子关系（parentId）。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * 查询当前用户的所有分类
     */
    @GetMapping
    public ResponseEntity<ApiResult<List<Category>>> getCategories(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Category> categories = categoryRepository.findByUserIdOrderBySortOrderAsc(user.getId());
        return ResponseEntity.ok(ApiResult.success(categories));
    }

    /**
     * 创建分类
     */
    @PostMapping
    public ResponseEntity<ApiResult<Category>> createCategory(
            Authentication authentication, @RequestBody CategoryRequest request) {

        User user = (User) authentication.getPrincipal();
        log.info("创建分类: userId={}, name={}", user.getId(), request.getName());

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder());
        category.setUser(user);

        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(ApiResult.success(saved));
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {

        User user = (User) authentication.getPrincipal();

        return categoryRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(category -> {
                    category.setName(request.getName());
                    category.setDescription(request.getDescription());
                    category.setIcon(request.getIcon());
                    category.setParentId(request.getParentId());
                    category.setSortOrder(request.getSortOrder());

                    Category updated = categoryRepository.save(category);
                    return (ResponseEntity<?>) ResponseEntity.ok(ApiResult.success(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();

        return categoryRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(category -> {
                    categoryRepository.delete(category);
                    log.info("删除分类: categoryId={}", id);
                    return (ResponseEntity<?>) ResponseEntity.ok(ApiResult.success());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /* ========== DTO ========== */

    @Data
    public static class CategoryRequest {
        private String name;
        private String description;
        private String icon;
        private Long parentId;
        private Integer sortOrder;
    }
}
