package com.inventorymanagement.ims.purchaseorder;

public record CreatePurchaseOrderItemDto(
        Long productId,
        int quantity
) {
}