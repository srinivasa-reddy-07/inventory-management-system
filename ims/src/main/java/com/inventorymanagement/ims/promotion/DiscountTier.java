package com.inventorymanagement.ims.promotion;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
public class DiscountTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int minQuantity;

    private BigDecimal pricePerItem;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
}