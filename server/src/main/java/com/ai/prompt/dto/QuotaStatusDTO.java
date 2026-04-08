package com.ai.prompt.dto;

import lombok.Data;

/**
 * 用户配额状态 DTO
 *
 * @author 马可行
 * @since 1.2.0
 */
@Data
public class QuotaStatusDTO {
    private Long userId;
    private Integer generateLimit;
    private Integer generateUsed;
    private Integer generateRemaining;
    private Integer favoriteLimit;
    private Integer favoriteUsed;
    private Integer favoriteRemaining;
    private Boolean canGenerate;
    private Boolean canFavorite;
}
