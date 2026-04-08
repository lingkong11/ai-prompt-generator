package com.ai.prompt.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户订阅信息 DTO
 *
 * @author 马可行
 * @since 1.2.0
 */
@Data
public class SubscriptionDTO {
    private Long id;
    private Long planId;
    private String planName;
    private String planCode;
    private String status;
    private String billingCycle;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private Boolean autoRenew;
    private Boolean isValid;
}
