package com.inventorymanagement.ims.promotion;

import com.inventorymanagement.ims.exception.ResourceNotFoundException;
import com.inventorymanagement.ims.product.Product;
import com.inventorymanagement.ims.product.ProductRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    public PromotionService(PromotionRepository promotionRepository, ProductRepository productRepository) {
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
    }

    public List<PromotionDto> getAllPromotions() {
        return promotionRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PromotionDto createPromotion(CreatePromotionDto createDto) {
        Product product = productRepository.findById(createDto.applicableProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + createDto.applicableProductId()));

        Promotion promotion = new Promotion();
        promotion.setDescription(createDto.description());
        promotion.setPromotionType(createDto.promotionType());
        promotion.setDiscountValue(createDto.discountValue());
        promotion.setApplicableProduct(product);

        Promotion savedPromotion = promotionRepository.save(promotion);
        return convertToDto(savedPromotion);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promotion not found with id: " + id);
        }
        promotionRepository.deleteById(id);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PromotionDto createTieredPromotion(CreateTieredPromotionDto createDto) {
        Product product = productRepository.findById(createDto.applicableProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + createDto.applicableProductId()));

        Promotion promotion = new Promotion();
        promotion.setDescription(createDto.description());
        promotion.setPromotionType(PromotionType.TIERED);
        promotion.setApplicableProduct(product);

        List<DiscountTier> tiers = new ArrayList<>();
        for (DiscountTierDto tierDto : createDto.tiers()) {
            DiscountTier tier = new DiscountTier();
            tier.setMinQuantity(tierDto.minQuantity());
            tier.setPricePerItem(tierDto.pricePerItem());
            tier.setPromotion(promotion); // Link the tier back to the promotion
            tiers.add(tier);
        }
        promotion.setDiscountTiers(tiers);

        Promotion savedPromotion = promotionRepository.save(promotion);
        return convertToDto(savedPromotion);
    }

    private PromotionDto convertToDto(Promotion promotion) {
        SimpleProductDto productDto = new SimpleProductDto(
                promotion.getApplicableProduct().getId(),
                promotion.getApplicableProduct().getName()
        );

        List<DiscountTierDto> tierDtos = new ArrayList<>();
        if (promotion.getDiscountTiers() != null) {
            tierDtos = promotion.getDiscountTiers().stream()
                    .map(tier -> new DiscountTierDto(tier.getMinQuantity(), tier.getPricePerItem()))
                    .toList();
        }

        return new PromotionDto(
                promotion.getId(),
                promotion.getDescription(),
                promotion.getPromotionType(),
                promotion.getDiscountValue(),
                productDto,
                tierDtos
        );
    }
}