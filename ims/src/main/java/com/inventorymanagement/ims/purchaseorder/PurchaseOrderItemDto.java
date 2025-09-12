package com.inventorymanagement.ims.purchaseorder;

public record PurchaseOrderItemDto(
        Long id,
        String productName,
        int quantity
) {
}