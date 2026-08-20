package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Transactional
public class ProductSeeder {

    private static final String CATEGORY_KEO_C1 = "Keo C1";

    private static final String CATEGORY_KEO_2 = "Keo 2";

    private static final Set<String> CATEGORIES = Set.of(
            CATEGORY_KEO_C1,
            CATEGORY_KEO_2
    );

    private final ProductRepository repository;

    private final SampleDataFactory factory;

    public void seed() {

        if (repository.count() > 0) {
            repository.findAll().forEach(product -> {
                product.setCategory(
                        normalizeCategory(product.getCategory())
                );
                if (product.getAveragePrice() == null) {
                    product.setAveragePrice(BigDecimal.ZERO);
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
                                CATEGORY_KEO_C1
                        ),

                        factory.product(
                                "FP002",
                                "Office Desk",
                                "Wooden desk",
                                "Piece",
                                CATEGORY_KEO_C1
                        ),

                        factory.product(
                                "FP003",
                                "Metal Shelf",
                                "5-layer shelf",
                                "Piece",
                                CATEGORY_KEO_C1
                        ),

                        factory.product(
                                "FP004",
                                "Tool Box",
                                "Heavy duty",
                                "Piece",
                                CATEGORY_KEO_2
                        ),

                        factory.product(
                                "FP005",
                                "Electrical Panel",
                                "220V",
                                "Piece",
                                CATEGORY_KEO_2
                        )

                )
                .filter(fp -> !repository.existsByCode(fp.getCode()))
                .forEach(repository::save);

        System.out.println("✓ Finished products seeded");

    }

    /**
     * Đưa phân loại cũ về bộ phân loại hiện hành (Keo C1 / Keo 2).
     */
    private String normalizeCategory(String category) {

        if (category == null || category.isBlank()) {
            return CATEGORY_KEO_C1;
        }

        if (CATEGORIES.contains(category)) {
            return category;
        }

        return category.contains("2")
                ? CATEGORY_KEO_2
                : CATEGORY_KEO_C1;

    }

}
