package com.inventorymanagement.ims.purchaseorder;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/purchaseorders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDto> createPurchaseOrder(@RequestBody CreatePurchaseOrderDto createDto) {
        PurchaseOrderDto createdOrder = purchaseOrderService.createOrder(createDto);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    // TODO: We can add endpoints to get all orders or a single order later

    @PutMapping("/{id}/status")
    public ResponseEntity<PurchaseOrderDto> updateOrderStatus(@PathVariable Long id, @RequestBody String status) {
        // A simple way to convert the string from the request to our Enum
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(purchaseOrderService.updateOrderStatus(id, newStatus));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderDto>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllOrders());
    }
}