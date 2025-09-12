package com.inventorymanagement.ims.analytics;

import java.math.BigDecimal;

public record DashboardStatsDto(
        long totalProducts,
        BigDecimal totalInventoryValue,
        long pendingOrders
) {
}