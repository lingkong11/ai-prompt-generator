package com.ai.prompt.controller;

import com.ai.prompt.entity.Category;
import com.ai.prompt.entity.User;
import com.ai.prompt.repository.CategoryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 分类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
    
    @GetMapping
    public ResponseEntity<?> getCategories(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Category> categories = categoryRepository.findByUserIdOrderBySortOrderAsc(user.getId());
        return ResponseEntity.ok(Map.of("categories", categories));
    }
    
    @PostMapping
    public ResponseEntity<?> createCategory(Authentication authentication, @RequestBody CategoryRequest request) {
        User user = (User) authentication.getPrincipal();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        log.info("[时间戳:{}] [阶段:Controller] [任务:创建分类] [动作:create] - name:{}", timestamp, request.getName());
        
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder());
        category.setUser(user);
        
        Category saved = categoryRepository.save(category);
        
        return ResponseEntity.ok(Map.of(
            "timestamp", timestamp,
            "status", "success",
            "category", saved
        ));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(Authentication authentication, 
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
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "category", updated
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        
        return categoryRepository.findById(id)
            .filter(c -> c.getUser().getId().equals(user.getId()))
            .map(category -> {
                categoryRepository.delete(category);
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "删除成功"
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @Data
    public static class CategoryRequest {
        private String name;
        private String description;
        private String icon;
        private Long parentId;
        private Integer sortOrder;
    }
}
