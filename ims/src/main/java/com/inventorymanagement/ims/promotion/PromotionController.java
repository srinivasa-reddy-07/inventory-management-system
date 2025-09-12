package com.inventorymanagement.ims.promotion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ResponseEntity<List<PromotionDto>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @PostMapping
    public ResponseEntity<PromotionDto> createPromotion(@RequestBody CreatePromotionDto createDto) {
        PromotionDto createdPromotion = promotionService.createPromotion(createDto);
        return new ResponseEntity<>(createdPromotion, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tiered")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionDto> createTieredPromotion(@RequestBody CreateTieredPromotionDto createDto) {
        PromotionDto createdPromotion = promotionService.createTieredPromotion(createDto);
        return new ResponseEntity<>(createdPromotion, HttpStatus.CREATED);
    }
}