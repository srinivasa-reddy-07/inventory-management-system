package com.inventorymanagement.ims.promotion;

import java.util.List;

public record CreateTieredPromotionDto(
        String description,
        Long applicableProductId,
        List<DiscountTierDto> tiers
) {
}