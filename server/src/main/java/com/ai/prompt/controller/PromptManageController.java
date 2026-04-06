package com.ai.prompt.controller;

import com.ai.prompt.entity.Category;
import com.ai.prompt.entity.Prompt;
import com.ai.prompt.entity.User;
import com.ai.prompt.repository.CategoryRepository;
import com.ai.prompt.repository.PromptRepository;
import com.ai.prompt.service.PromptService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 提示词管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptManageController {
    
    private final PromptRepository promptRepository;
    private final CategoryRepository categoryRepository;
    private final PromptService promptService;
    
    @PostMapping
    public ResponseEntity<?> createPrompt(Authentication authentication, @RequestBody PromptCreateRequest request) {
        User user = (User) authentication.getPrincipal();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        log.info("[时间戳:{}] [阶段:Controller] [任务:创建提示词] [动作:create] - userId:{} title:{}", timestamp, user.getId(), request.getTitle());
        
        Prompt prompt = new Prompt();
        prompt.setTitle(request.getTitle());
        prompt.setContent(request.getContent());
        prompt.setType(request.getType());
        prompt.setStyle(request.getStyle());
        prompt.setLanguage(request.getLanguage());
        prompt.setGoal(request.getGoal());
        prompt.setTags(request.getTags());
        prompt.setUser(user);
        prompt.setIsFavorite(false);
        prompt.setIsTemplate(false);
        prompt.setUseCount(0);
        
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId()).ifPresent(prompt::setCategory);
        }
        
        Prompt saved = promptRepository.save(prompt);
        
        return ResponseEntity.ok(Map.of(
            "timestamp", timestamp,
            "status", "success",
            "prompt", saved
        ));
    }
    
    @GetMapping
    public ResponseEntity<?> getPrompts(Authentication authentication,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) String type,
                                        @RequestParam(required = false) String keyword) {
        User user = (User) authentication.getPrincipal();
        
        Page<Prompt> prompts;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (keyword != null && !keyword.isEmpty()) {
            prompts = promptRepository.searchByUserIdAndKeyword(user.getId(), keyword, pageRequest);
        } else if (type != null && !type.isEmpty()) {
            prompts = promptRepository.findByUserIdAndType(user.getId(), type, pageRequest);
        } else {
            prompts = promptRepository.findByUserId(user.getId(), pageRequest);
        }
        
        return ResponseEntity.ok(Map.of(
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "prompts", prompts.getContent(),
            "totalElements", prompts.getTotalElements(),
            "totalPages", prompts.getTotalPages(),
            "currentPage", prompts.getNumber()
        ));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPrompt(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        
        return promptRepository.findById(id)
            .filter(p -> p.getUser().getId().equals(user.getId()))
            .map(prompt -> ResponseEntity.ok(Map.of("prompt", prompt)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePrompt(Authentication authentication, 
                                          @PathVariable Long id, 
                                          @RequestBody PromptCreateRequest request) {
        User user = (User) authentication.getPrincipal();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        
        return promptRepository.findById(id)
            .filter(p -> p.getUser().getId().equals(user.getId()))
            .map(prompt -> {
                prompt.setTitle(request.getTitle());
                prompt.setContent(request.getContent());
                prompt.setType(request.getType());
                prompt.setStyle(request.getStyle());
                prompt.setLanguage(request.getLanguage());
                prompt.setGoal(request.getGoal());
                prompt.setTags(request.getTags());
                
                if (request.getCategoryId() != null) {
                    categoryRepository.findById(request.getCategoryId()).ifPresent(prompt::setCategory);
                }
                
                Prompt updated = promptRepository.save(prompt);
                
                log.info("[时间戳:{}] [阶段:Controller] [任务:更新提示词] [动作:update] - promptId:{}", timestamp, id);
                
                return ResponseEntity.ok(Map.of(
                    "timestamp", timestamp,
                    "status", "success",
                    "prompt", updated
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePrompt(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        
        return promptRepository.findById(id)
            .filter(p -> p.getUser().getId().equals(user.getId()))
            .map(prompt -> {
                promptRepository.delete(prompt);
                log.info("[时间戳:{}] [阶段:Controller] [任务:删除提示词] [动作:delete] - promptId:{}", timestamp, id);
                return ResponseEntity.ok(Map.of(
                    "timestamp", timestamp,
                    "status", "success",
                    "message", "删除成功"
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        
        return promptRepository.findById(id)
            .filter(p -> p.getUser().getId().equals(user.getId()))
            .map(prompt -> {
                prompt.setIsFavorite(!prompt.getIsFavorite());
                promptRepository.save(prompt);
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "isFavorite", prompt.getIsFavorite()
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Prompt> favorites = promptRepository.findByUserIdAndIsFavoriteTrue(user.getId());
        return ResponseEntity.ok(Map.of("prompts", favorites));
    }
    
    @PostMapping("/{id}/use")
    public ResponseEntity<?> incrementUseCount(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        
        return promptRepository.findById(id)
            .filter(p -> p.getUser().getId().equals(user.getId()))
            .map(prompt -> {
                prompt.setUseCount(prompt.getUseCount() + 1);
                promptRepository.save(prompt);
                return ResponseEntity.ok(Map.of("useCount", prompt.getUseCount()));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @Data
    public static class PromptCreateRequest {
        private String title;
        private String content;
        private String type;
        private String style;
        private String language;
        private String goal;
        private String tags;
        private Long categoryId;
    }
}
