package com.inventorymanagement.ims.product;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;
import lombok.Data;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "products")
public class Product {

    private boolean isBundle = false;

    @ManyToMany
    @JoinTable(
            name = "product_bundles",
            joinColumns = @JoinColumn(name = "bundle_id"),
            inverseJoinColumns = @JoinColumn(name = "component_id")
    )
    private Set<Product> bundledProducts = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "NAME")
    @Column(unique = true)
    private String name;

    @CsvBindByName(column = "DESCRIPTION")
    private String description;

    @CsvBindByName(column = "PRICE")
    private double price;

    @CsvBindByName(column = "QUANTITY")
    private int quantity;

    @CsvBindByName(column = "PRODUCT_SIZE")
    @Column(name = "product_size")
    private String size;

    @CsvBindByName(column = "PRODUCT_COLOR")
    @Column(name = "product_color")
    private String color;
}