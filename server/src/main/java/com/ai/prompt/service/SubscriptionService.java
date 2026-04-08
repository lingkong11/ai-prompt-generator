package com.ai.prompt.service;

import com.ai.prompt.dto.PlanDTO;
import com.ai.prompt.dto.QuotaStatusDTO;
import com.ai.prompt.dto.SubscriptionDTO;
import com.ai.prompt.entity.QuotaUsage;
import com.ai.prompt.entity.SubscriptionPlan;
import com.ai.prompt.entity.UserSubscription;
import com.ai.prompt.repository.QuotaUsageRepository;
import com.ai.prompt.repository.SubscriptionPlanRepository;
import com.ai.prompt.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订阅服务层
 *
 * <p>处理套餐查询、订阅管理、配额校验等核心业务逻辑。</p>
 *
 * @author 马可行
 * @since 1.2.0
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final QuotaUsageRepository quotaUsageRepository;

    /**
     * 获取所有启用的套餐
     */
    public List<PlanDTO> getAllPlans() {
        return planRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::convertToPlanDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户当前订阅
     */
    public SubscriptionDTO getCurrentSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId)
                .map(this::convertToSubscriptionDTO)
                .orElseGet(() -> createFreeSubscription(userId));
    }

    /**
     * 获取用户配额状态
     */
    public QuotaStatusDTO getQuotaStatus(Long userId) {
        SubscriptionDTO subscription = getCurrentSubscription(userId);
        SubscriptionPlan plan = planRepository.findByCode(subscription.getPlanCode())
                .orElseThrow(() -> new RuntimeException("套餐不存在: " + subscription.getPlanCode()));

        QuotaStatusDTO dto = new QuotaStatusDTO();
        dto.setUserId(userId);
        dto.setGenerateLimit(plan.getGenerateLimit());
        dto.setFavoriteLimit(plan.getFavoriteLimit());

        // 获取今日配额使用
        QuotaUsage usage = quotaUsageRepository
                .findByUserIdAndUsageDate(userId, LocalDate.now())
                .orElseGet(() -> createQuotaUsage(userId));

        dto.setGenerateUsed(usage.getGenerateCount());
        dto.setFavoriteUsed(usage.getFavoriteCount());

        // 计算剩余配额
        dto.setGenerateRemaining(plan.getGenerateLimit() == -1 ? -1 
                : Math.max(0, plan.getGenerateLimit() - usage.getGenerateCount()));
        dto.setFavoriteRemaining(plan.getFavoriteLimit() == -1 ? -1 
                : Math.max(0, plan.getFavoriteLimit() - usage.getFavoriteCount()));

        dto.setCanGenerate(plan.getGenerateLimit() == -1 || dto.getGenerateRemaining() > 0);
        dto.setCanFavorite(plan.getFavoriteLimit() == -1 || dto.getFavoriteRemaining() > 0);

        return dto;
    }

    /**
     * 检查并消耗生成配额
     */
    @Transactional
    public boolean consumeGenerateQuota(Long userId) {
        QuotaStatusDTO status = getQuotaStatus(userId);
        if (!status.getCanGenerate()) {
            return false;
        }

        QuotaUsage usage = quotaUsageRepository
                .findByUserIdAndUsageDate(userId, LocalDate.now())
                .orElseGet(() -> createQuotaUsage(userId));
        
        usage.incrementGenerate();
        quotaUsageRepository.save(usage);
        return true;
    }

    /**
     * 检查并消耗收藏配额
     */
    @Transactional
    public boolean consumeFavoriteQuota(Long userId) {
        QuotaStatusDTO status = getQuotaStatus(userId);
        if (!status.getCanFavorite()) {
            return false;
        }

        QuotaUsage usage = quotaUsageRepository
                .findByUserIdAndUsageDate(userId, LocalDate.now())
                .orElseGet(() -> createQuotaUsage(userId));
        
        usage.incrementFavorite();
        quotaUsageRepository.save(usage);
        return true;
    }

    /**
     * 释放收藏配额（取消收藏时）
     */
    @Transactional
    public void releaseFavoriteQuota(Long userId) {
        quotaUsageRepository.findByUserIdAndUsageDate(userId, LocalDate.now())
                .ifPresent(usage -> {
                    usage.decrementFavorite();
                    quotaUsageRepository.save(usage);
                });
    }

    /**
     * 升级套餐（Mock实现，实际需接入支付）
     */
    @Transactional
    public SubscriptionDTO upgradePlan(Long userId, String planCode, String billingCycle) {
        SubscriptionPlan plan = planRepository.findByCode(planCode)
                .orElseThrow(() -> new RuntimeException("套餐不存在: " + planCode));

        // 取消当前订阅
        subscriptionRepository.findActiveByUserId(userId)
                .ifPresent(sub -> {
                    sub.setStatus("CANCELED");
                    subscriptionRepository.save(sub);
                });

        // 创建新订阅
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setPlanId(plan.getId());
        subscription.setStatus("ACTIVE");
        subscription.setBillingCycle(billingCycle);
        subscription.setStartedAt(java.time.LocalDateTime.now());
        // 设置到期时间（实际应根据支付结果）
        subscription.setExpiresAt(java.time.LocalDateTime.now().plusMonths("YEARLY".equals(billingCycle) ? 12 : 1));
        subscription.setAutoRenew(true);

        subscriptionRepository.save(subscription);
        return convertToSubscriptionDTO(subscription);
    }

    /**
     * 取消订阅
     */
    @Transactional
    public void cancelSubscription(Long userId) {
        subscriptionRepository.findActiveByUserId(userId)
                .ifPresent(sub -> {
                    sub.setStatus("CANCELED");
                    sub.setCanceledAt(java.time.LocalDateTime.now());
                    sub.setAutoRenew(false);
                    subscriptionRepository.save(sub);
                });
    }

    // ========== 私有方法 ==========

    private PlanDTO convertToPlanDTO(SubscriptionPlan plan) {
        PlanDTO dto = new PlanDTO();
        dto.setId(plan.getId());
        dto.setCode(plan.getCode());
        dto.setName(plan.getName());
        dto.setPriceMonthly(plan.getPriceMonthly());
        dto.setPriceYearly(plan.getPriceYearly());
        dto.setGenerateLimit(plan.getGenerateLimit());
        dto.setFavoriteLimit(plan.getFavoriteLimit());
        dto.setFeatures(plan.getFeatures() != null 
                ? Arrays.asList(plan.getFeatures().split(",")) 
                : List.of());
        dto.setIsPopular("PRO".equals(plan.getCode()));
        return dto;
    }

    private SubscriptionDTO convertToSubscriptionDTO(UserSubscription sub) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(sub.getId());
        dto.setPlanId(sub.getPlanId());
        dto.setStatus(sub.getStatus());
        dto.setBillingCycle(sub.getBillingCycle());
        dto.setStartedAt(sub.getStartedAt());
        dto.setExpiresAt(sub.getExpiresAt());
        dto.setAutoRenew(sub.getAutoRenew());
        dto.setIsValid(sub.isValid());

        // 查询套餐信息
        planRepository.findById(sub.getPlanId())
                .ifPresent(plan -> {
                    dto.setPlanName(plan.getName());
                    dto.setPlanCode(plan.getCode());
                });

        return dto;
    }

    private SubscriptionDTO createFreeSubscription(Long userId) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setPlanId(1L);
        dto.setPlanName("免费版");
        dto.setPlanCode("FREE");
        dto.setStatus("ACTIVE");
        dto.setIsValid(true);
        dto.setAutoRenew(false);
        return dto;
    }

    private QuotaUsage createQuotaUsage(Long userId) {
        QuotaUsage usage = new QuotaUsage();
        usage.setUserId(userId);
        usage.setUsageDate(LocalDate.now());
        usage.setGenerateCount(0);
        usage.setFavoriteCount(0);
        return quotaUsageRepository.save(usage);
    }
}
