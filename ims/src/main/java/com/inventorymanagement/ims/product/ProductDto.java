package com.inventorymanagement.ims.product;

import java.util.Set;

public record ProductDto(
        Long id,
        String name,
        String description,
        double price,
        int quantity,
        String size,
        String color,
        boolean isBundle,
        Set<BundledProductDto> bundledProducts
) {
}