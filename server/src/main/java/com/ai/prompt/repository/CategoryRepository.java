package com.ai.prompt.repository;

import com.ai.prompt.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByUserId(Long userId);
    
    List<Category> findByUserIdOrderBySortOrderAsc(Long userId);
    
    List<Category> findByParentId(Long parentId);
}
