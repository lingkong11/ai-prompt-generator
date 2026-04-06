package com.ai.prompt.repository;

import com.ai.prompt.entity.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {
    
    List<Prompt> findByUserId(Long userId);
    
    List<Prompt> findByUserIdAndIsFavoriteTrue(Long userId);
    
    List<Prompt> findByUserIdAndIsTemplateTrue(Long userId);
    
    List<Prompt> findByCategoryId(Long categoryId);
    
    Page<Prompt> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT p FROM Prompt p WHERE p.user.id = ?1 AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
           "LOWER(p.tags) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<Prompt> searchByUserIdAndKeyword(Long userId, String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Prompt p WHERE p.user.id = ?1 AND p.type = ?2")
    Page<Prompt> findByUserIdAndType(Long userId, String type, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Prompt p WHERE p.user.id = ?1")
    long countByUserId(Long userId);
}
