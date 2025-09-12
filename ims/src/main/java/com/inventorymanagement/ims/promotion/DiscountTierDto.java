package com.inventorymanagement.ims.promotion;

import java.math.BigDecimal;

public record DiscountTierDto(
        int minQuantity,
        BigDecimal pricePerItem
) {
}