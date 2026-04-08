package com.ai.prompt.repository;

import com.ai.prompt.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 套餐数据访问层
 *
 * @author 马可行
 * @since 1.2.0
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    /**
     * 根据编码查找套餐
     */
    Optional<SubscriptionPlan> findByCode(String code);

    /**
     * 查找所有启用的套餐，按排序权重排序
     */
    List<SubscriptionPlan> findByIsActiveTrueOrderBySortOrderAsc();
}
