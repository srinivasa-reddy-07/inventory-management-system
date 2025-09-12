package com.inventorymanagement.ims.purchaseorder;

import java.util.List;

public record CreatePurchaseOrderDto(
        List<CreatePurchaseOrderItemDto> items
) {
}