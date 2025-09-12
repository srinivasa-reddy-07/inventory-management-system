package com.inventorymanagement.ims.purchaseorder;

import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderDto(
        Long id,
        LocalDateTime orderDate,
        OrderStatus status,
        List<PurchaseOrderItemDto> items
) {
}