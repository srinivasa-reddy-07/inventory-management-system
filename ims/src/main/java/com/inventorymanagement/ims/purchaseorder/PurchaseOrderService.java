package com.inventorymanagement.ims.purchaseorder;

import com.inventorymanagement.ims.exception.ResourceNotFoundException;
import com.inventorymanagement.ims.product.Product;
import com.inventorymanagement.ims.product.ProductRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository, ProductRepository productRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PurchaseOrderDto createOrder(CreatePurchaseOrderDto createDto) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setOrderDate(LocalDateTime.now());
        purchaseOrder.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CreatePurchaseOrderItemDto itemDto : createDto.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.productId()));

            if (product.isBundle()) {
                for (Product component : product.getBundledProducts()) {
                    if (component.getQuantity() < itemDto.quantity()) {
                        throw new IllegalStateException("Not enough stock for product: " + product.getName());
                    }
                    component.setQuantity(component.getQuantity() - itemDto.quantity());
                }
            } else {

                if (product.getQuantity() < itemDto.quantity()) {
                    throw new IllegalStateException("Not enough stock for product : " + product.getName());
                }

                product.setQuantity(product.getQuantity() - itemDto.quantity());
            }


            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.quantity());
            orderItem.setPurchaseOrder(purchaseOrder); // Link item to the order
            orderItems.add(orderItem);
        }

        purchaseOrder.setItems(orderItems);
        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);
        return convertToDto(savedOrder);
    }

    public List<PurchaseOrderDto> getAllOrders() {
        return purchaseOrderRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PurchaseOrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(status);
        PurchaseOrder updatedOrder = purchaseOrderRepository.save(order);
        return convertToDto(updatedOrder);
    }

    // --- Private DTO Methods ---
    private PurchaseOrderDto convertToDto(PurchaseOrder order) {
        List<PurchaseOrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new PurchaseOrderItemDto(
                        item.getId(),
                        item.getProduct().getName(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        return new PurchaseOrderDto(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                itemDtos
        );
    }
}