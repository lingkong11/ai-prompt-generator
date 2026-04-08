package com.ai.prompt.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐信息 DTO
 *
 * @author 马可行
 * @since 1.2.0
 */
@Data
public class PlanDTO {
    private Long id;
    private String code;
    private String name;
    private BigDecimal priceMonthly;
    private BigDecimal priceYearly;
    private Integer generateLimit;
    private Integer favoriteLimit;
    private List<String> features;
    private Boolean isPopular;
}
