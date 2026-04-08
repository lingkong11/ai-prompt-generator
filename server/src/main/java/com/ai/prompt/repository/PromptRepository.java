package com.ai.prompt.repository;

import com.ai.prompt.entity.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 提示词数据访问层
 */
@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {

    /** 查询指定用户的全部提示词（不分页） */
    List<Prompt> findByUserId(Long userId);

    /** 查询指定用户的收藏列表 */
    List<Prompt> findByUserIdAndIsFavoriteTrue(Long userId);

    /** 查询指定用户标记为模板的提示词 */
    List<Prompt> findByUserIdAndIsTemplateTrue(Long userId);

    /** 按分类查询 */
    List<Prompt> findByCategoryId(Long categoryId);

    /** 分页查询指定用户的提示词，按 createdAt 降序 */
    Page<Prompt> findByUserId(Long userId, Pageable pageable);

    /** 全文模糊搜索（标题 / 内容 / 标签），不区分大小写 */
    @Query("SELECT p FROM Prompt p WHERE p.user.id = ?1 AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
           "LOWER(p.tags) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<Prompt> searchByUserIdAndKeyword(Long userId, String keyword, Pageable pageable);

    /** 按类型分页过滤 */
    @Query("SELECT p FROM Prompt p WHERE p.user.id = ?1 AND p.type = ?2")
    Page<Prompt> findByUserIdAndType(Long userId, String type, Pageable pageable);

    /** 统计指定用户的提示词总数 */
    @Query("SELECT COUNT(p) FROM Prompt p WHERE p.user.id = ?1")
    long countByUserId(Long userId);
}
