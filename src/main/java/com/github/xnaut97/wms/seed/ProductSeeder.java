package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Transactional
public class ProductSeeder {

    private final ProductRepository repository;

    private final SampleDataFactory factory;

    public void seed() {

        if (repository.count() > 0) {
            repository.findAll().forEach(product -> {
                if (product.getCategory() == null || product.getCategory().isBlank()) {
                    product.setCategory("Sản phẩm khác");
                }
                if (product.getAveragePrice() == null) {
                    product.setAveragePrice(product.getSellingPrice());
                }
                if (product.getMinimumStock() == null) {
                    product.setMinimumStock(BigDecimal.ZERO);
                }
                if (product.getMaximumStock() == null) {
                    product.setMaximumStock(BigDecimal.ZERO);
                }
                repository.save(product);
            });
            return;
        }

        Stream.of(

                        factory.product(
                                "FP001",
                                "Steel Cabinet",
                                "2-door cabinet",
                                "Piece",
                                new BigDecimal("1800000"),
                                "Sản phẩm khác"
                        ),

                        factory.product(
                                "FP002",
                                "Office Desk",
                                "Wooden desk",
                                "Piece",
                                new BigDecimal("2500000"),
                                "Sản phẩm khác"
                        ),

                        factory.product(
                                "FP003",
                                "Metal Shelf",
                                "5-layer shelf",
                                "Piece",
                                new BigDecimal("1450000"),
                                "Keo dán gạch"
                        ),

                        factory.product(
                                "FP004",
                                "Tool Box",
                                "Heavy duty",
                                "Piece",
                                new BigDecimal("650000"),
                                "Keo 2 thành phần"
                        ),

                        factory.product(
                                "FP005",
                                "Electrical Panel",
                                "220V",
                                "Piece",
                                new BigDecimal("3200000"),
                                "Sản phẩm khác"
                        )

                )
                .filter(fp -> !repository.existsByCode(fp.getCode()))
                .forEach(repository::save);

        System.out.println("✓ Finished products seeded");

    }

}
