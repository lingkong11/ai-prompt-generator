package com.ai.prompt.repository;

import com.ai.prompt.entity.QuotaUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 配额使用记录数据访问层
 *
 * @author 马可行
 * @since 1.2.0
 */
@Repository
public interface QuotaUsageRepository extends JpaRepository<QuotaUsage, Long> {

    /**
     * 查找用户指定日期的配额记录
     */
    Optional<QuotaUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);
}
