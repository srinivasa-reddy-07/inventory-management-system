package com.inventorymanagement.ims.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByQuantityLessThan(int quantity);

    Optional<Product> findByName(String name);

    @Query("SELECT SUM(p.price * p.quantity) FROM Product p")
    BigDecimal getTotalInventoryValue();

}
