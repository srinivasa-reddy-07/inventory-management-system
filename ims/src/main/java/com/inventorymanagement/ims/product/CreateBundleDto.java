package com.inventorymanagement.ims.product;

import java.util.List;

public record CreateBundleDto(
        Long bundleProductId,
        List<Long> componentProductIds
) {
}