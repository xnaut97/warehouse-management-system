package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductSeeder {

    public record Recipe(
            String code,
            String name,
            String specification,
            String category,
            BigDecimal salePrice,
            int outputWeight,
            BigDecimal minimumStock,
            BigDecimal maximumStock
    ) {
    }

    public static final String UNIT = "Bao";

    public static final String C1_CATEGORY = "Keo C1";

    public static final String C2_CATEGORY = "Keo 2";

    public static final List<Recipe> RECIPES = List.of(
            new Recipe(
                    "TP-C1-25",
                    "Keo dán gạch C1",
                    "Bao 25kg, keo dán gạch gốc xi măng, thi công nội thất",
                    C1_CATEGORY,
                    BigDecimal.valueOf(95000),
                    5,
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(20000)
            ),
            new Recipe(
                    "TP-C1-20",
                    "Keo dán gạch C1 chống trượt",
                    "Bao 20kg, keo dán gạch gốc xi măng, chống trượt cho tường đứng",
                    C1_CATEGORY,
                    BigDecimal.valueOf(82000),
                    3,
                    BigDecimal.valueOf(800),
                    BigDecimal.valueOf(15000)
            ),
            new Recipe(
                    "TP-C2-25",
                    "Keo dán gạch C2",
                    "Bao 25kg, keo dán gạch gốc xi măng cường độ cao, dùng cho gạch khổ lớn",
                    C2_CATEGORY,
                    BigDecimal.valueOf(135000),
                    2,
                    BigDecimal.valueOf(600),
                    BigDecimal.valueOf(12000)
            )
    );

    public static final String PRIMARY_CODE = RECIPES.getFirst().code();

    private final ProductRepository repository;

    private final SampleDataFactory factory;

    @Transactional
    public void seed() {

        for (Recipe recipe : RECIPES) {

            if (repository.existsByCode(recipe.code())) {
                continue;
            }

            repository.save(
                    factory.product(
                            recipe.code(),
                            recipe.name(),
                            recipe.specification(),
                            UNIT,
                            recipe.category(),
                            recipe.minimumStock(),
                            recipe.maximumStock()
                    )
            );

        }

        System.out.println("✓ Products seeded");

    }

}
