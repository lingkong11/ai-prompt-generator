package com.ai.prompt.controller;

import com.ai.prompt.common.ApiResult;
import com.ai.prompt.dto.PlanDTO;
import com.ai.prompt.dto.QuotaStatusDTO;
import com.ai.prompt.dto.SubscriptionDTO;
import com.ai.prompt.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订阅管理控制器
 *
 * <p>提供套餐查询、订阅管理、配额查询等 REST API。</p>
 *
 * @author 马可行
 * @since 1.2.0
 */
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 获取所有套餐列表
     */
    @GetMapping("/plans")
    public ApiResult<List<PlanDTO>> getPlans() {
        return ApiResult.success(subscriptionService.getAllPlans());
    }

    /**
     * 获取当前用户订阅
     */
    @GetMapping("/current")
    public ApiResult<SubscriptionDTO> getCurrentSubscription(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ApiResult.success(subscriptionService.getCurrentSubscription(userId));
    }

    /**
     * 升级套餐
     */
    @PostMapping("/upgrade")
    public ApiResult<SubscriptionDTO> upgradePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        Long userId = extractUserId(userDetails);
        String planCode = request.get("planCode");
        String billingCycle = request.getOrDefault("billingCycle", "MONTHLY");
        return ApiResult.success(subscriptionService.upgradePlan(userId, planCode, billingCycle));
    }

    /**
     * 取消订阅
     */
    @PostMapping("/cancel")
    public ApiResult<Void> cancelSubscription(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        subscriptionService.cancelSubscription(userId);
        return ApiResult.success(null);
    }

    /**
     * 获取配额状态
     */
    @GetMapping("/quota")
    public ApiResult<QuotaStatusDTO> getQuotaStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ApiResult.success(subscriptionService.getQuotaStatus(userId));
    }

    private Long extractUserId(UserDetails userDetails) {
        // 从 JWT 或 UserDetails 中提取用户ID
        // 简化实现：假设 username 就是 userId
        return Long.valueOf(userDetails.getUsername());
    }
}
