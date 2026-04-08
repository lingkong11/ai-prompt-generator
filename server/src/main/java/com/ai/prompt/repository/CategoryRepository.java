package com.ai.prompt.repository;

import com.ai.prompt.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分类数据访问层
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** 查询指定用户的全部分类 */
    List<Category> findByUserId(Long userId);

    /** 查询指定用户的分类，按 sortOrder 升序排列 */
    List<Category> findByUserIdOrderBySortOrderAsc(Long userId);

    /** 查询某分类下的子分类 */
    List<Category> findByParentId(Long parentId);
}
