package com.inventorymanagement.ims.promotion;

import com.inventorymanagement.ims.product.Product;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    private PromotionType promotionType;

    private BigDecimal discountValue;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product applicableProduct;

    @OneToMany(
            mappedBy = "promotion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DiscountTier> discountTiers = new ArrayList<>();
}