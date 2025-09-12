package com.inventorymanagement.ims.product;

import com.inventorymanagement.ims.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private static final int LOW_STOCK_THRESHOLD = 10;

    public List<ProductDto> findLowStockProducts() {
        return productRepository.findByQuantityLessThan(LOW_STOCK_THRESHOLD)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void importProductsFromCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CsvToBean<Product> csvToBean = new CsvToBeanBuilder<Product>(reader)
                    .withType(Product.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<Product> csvProducts = csvToBean.parse();

            for (Product csvProduct : csvProducts) {
                // Check if a product with the same name already exists
                Optional<Product> existingProductOpt = productRepository.findByName(csvProduct.getName());

                if (existingProductOpt.isPresent()) {
                    // If it exists, UPDATE the existing product
                    Product dbProduct = existingProductOpt.get();
                    dbProduct.setDescription(csvProduct.getDescription());
                    dbProduct.setPrice(csvProduct.getPrice());
                    dbProduct.setQuantity(csvProduct.getQuantity());
                    dbProduct.setSize(csvProduct.getSize());
                    dbProduct.setColor(csvProduct.getColor());
                    productRepository.save(dbProduct); // This will be an UPDATE
                } else {
                    // If it does not exist, INSERT the new product
                    productRepository.save(csvProduct); // This will be an INSERT
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import CSV data: " + e.getMessage());
        }
    }

    public void exportProductsToCsv(Writer writer) {
        try {
            List<Product> products = productRepository.findAll();
            StatefulBeanToCsv<Product> beanToCsv = new StatefulBeanToCsvBuilder<Product>(writer).build();
            beanToCsv.write(products);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export CSV data: " + e.getMessage());
        }
    }

    // --- Public methods now use DTOs ---

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id).map(this::convertToDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto addProduct(ProductDto productDto) {
        Product product = convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Update entity from DTO
        product.setName(productDto.name());
        product.setDescription(productDto.description());
        product.setPrice(productDto.price());
        product.setQuantity(productDto.quantity());
        product.setSize(productDto.size());
        product.setColor(productDto.color());

        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto createOrUpdateBundle(CreateBundleDto createBundleDto) {
        // Find the product that will become the bundle
        Product bundleProduct = productRepository.findById(createBundleDto.bundleProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Bundle product not found with id: " + createBundleDto.bundleProductId()));

        bundleProduct.setBundle(true); // Mark it as a bundle
        bundleProduct.getBundledProducts().clear(); // Clear existing components for updates

        // Find and add all component products
        for (Long componentId : createBundleDto.componentProductIds()) {
            Product componentProduct = productRepository.findById(componentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Component product not found with id: " + componentId));
            bundleProduct.getBundledProducts().add(componentProduct);
        }

        Product savedBundle = productRepository.save(bundleProduct);
        return convertToDto(savedBundle);
    }

    // --- Private mapping methods ---

    private ProductDto convertToDto(Product product) {
        Set<BundledProductDto> bundledDtos = new HashSet<>();
        if (product.isBundle() && product.getBundledProducts() != null) {
            bundledDtos = product.getBundledProducts().stream()
                    .map(p -> new BundledProductDto(p.getId(), p.getName()))
                    .collect(Collectors.toSet());
        }

        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getSize(),
                product.getColor(),
                product.isBundle(),
                bundledDtos
        );
    }

    private Product convertToEntity(ProductDto productDto) {
        Product product = new Product();
        // No need to set the ID here because it's generated by the database for new products
        product.setName(productDto.name());
        product.setDescription(productDto.description());
        product.setPrice(productDto.price());
        product.setQuantity(productDto.quantity());
        product.setSize(productDto.size());
        product.setColor(productDto.color());
        return product;
    }
}