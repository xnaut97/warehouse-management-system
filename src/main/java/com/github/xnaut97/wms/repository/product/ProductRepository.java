package com.github.xnaut97.wms.repository.product;

import com.github.xnaut97.wms.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    boolean existsByCode(String code);

    Optional<Product> findByCode(String code);

    Page<Product>
    findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String code,
            String name,
            String category,
            Pageable pageable
    );
}