package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.FinishedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Transactional
public class FinishedProductSeeder {

    private final FinishedProductRepository repository;

    private final SampleDataFactory factory;

    public void seed() {

        if (repository.count() > 0) {
            return;
        }

        Stream.of(

                        factory.finishedProduct(
                                "FP001",
                                "Steel Cabinet",
                                "2-door cabinet",
                                "Piece",
                                new BigDecimal("1800000")
                        ),

                        factory.finishedProduct(
                                "FP002",
                                "Office Desk",
                                "Wooden desk",
                                "Piece",
                                new BigDecimal("2500000")
                        ),

                        factory.finishedProduct(
                                "FP003",
                                "Metal Shelf",
                                "5-layer shelf",
                                "Piece",
                                new BigDecimal("1450000")
                        ),

                        factory.finishedProduct(
                                "FP004",
                                "Tool Box",
                                "Heavy duty",
                                "Piece",
                                new BigDecimal("650000")
                        ),

                        factory.finishedProduct(
                                "FP005",
                                "Electrical Panel",
                                "220V",
                                "Piece",
                                new BigDecimal("3200000")
                        )

                )
                .filter(fp -> !repository.existsByCode(fp.getCode()))
                .forEach(repository::save);

        System.out.println("✓ Finished products seeded");

    }

}