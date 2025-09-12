package com.inventorymanagement.ims.promotion;

import java.math.BigDecimal;

public record CreatePromotionDto(
        String description,
        PromotionType promotionType,
        BigDecimal discountValue,
        // Client only sends the ID of the product rather than the entire Product object
        Long applicableProductId
) {
}