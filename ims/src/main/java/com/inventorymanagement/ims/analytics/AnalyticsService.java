package com.inventorymanagement.ims.analytics;

import com.inventorymanagement.ims.product.ProductRepository;
import com.inventorymanagement.ims.purchaseorder.OrderStatus;
import com.inventorymanagement.ims.purchaseorder.PurchaseOrderRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AnalyticsService {
    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public AnalyticsService(ProductRepository productRepository, PurchaseOrderRepository purchaseOrderRepository) {
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        long totalProducts = productRepository.count();
        BigDecimal totalInventoryValue = productRepository.getTotalInventoryValue();
        long pendingOrders = purchaseOrderRepository.countByStatus(OrderStatus.PENDING);

        return new DashboardStatsDto(totalProducts, totalInventoryValue, pendingOrders);
    }
}
