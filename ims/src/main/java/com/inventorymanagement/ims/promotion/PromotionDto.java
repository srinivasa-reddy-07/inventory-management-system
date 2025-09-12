package com.inventorymanagement.ims.promotion;

import java.math.BigDecimal;
import java.util.List;

// A simple DTO to represent product info within the promotion response
record SimpleProductDto(Long id, String name) {}

public record PromotionDto(
        Long id,
        String description,
        PromotionType promotionType,
        BigDecimal discountValue, // This will be null for tiered promotions
        SimpleProductDto applicableProduct,
        List<DiscountTierDto> discountTiers
) {
}