package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductSeeder {

    public record Recipe(
            String code,
            String name,
            String specification,
            String category,
            String lotPrefix,
            String packagingCode,
            String packagingName,
            BigDecimal packagingPrice,
            BigDecimal salePrice,
            int outputWeight,
            boolean premium,
            BigDecimal minimumStock,
            BigDecimal maximumStock
    ) {
    }

    public static final String UNIT = "Bao";

    public static final String C1_CATEGORY = "Keo C1";

    public static final String C2_CATEGORY = "Keo 2";

    public static final List<Recipe> RECIPES = List.of(
            new Recipe(
                    "MAT-C1-25X",
                    "Mastik Pro 25Kg Xanh",
                    "Bao 25kg, keo dán gạch C1 gốc xi măng",
                    C1_CATEGORY,
                    "MTX25",
                    "PKG-MAT-25X",
                    "Vỏ bao Keo Mastik Pro 25Kg Xanh",
                    BigDecimal.valueOf(4200),
                    BigDecimal.valueOf(95000),
                    6,
                    false,
                    BigDecimal.valueOf(3000),
                    BigDecimal.valueOf(15000)
            ),
            new Recipe(
                    "MAT-C1-25V",
                    "Mastik Pro 25Kg Vàng",
                    "Bao 25kg, keo dán gạch C1 gốc xi măng",
                    C1_CATEGORY,
                    "MTV25",
                    "PKG-MAT-25V",
                    "Vỏ bao Keo Mastik Pro 25Kg Vàng",
                    BigDecimal.valueOf(4200),
                    BigDecimal.valueOf(96000),
                    5,
                    false,
                    BigDecimal.valueOf(3000),
                    BigDecimal.valueOf(15000)
            ),
            new Recipe(
                    "PUT-C1-25D",
                    "PuThai 25Kg Đỏ",
                    "Bao 25kg, keo dán gạch C1 gốc xi măng",
                    C1_CATEGORY,
                    "PTD25",
                    "PKG-PUT-25D",
                    "Vỏ bao Keo PuThai 25Kg Đỏ",
                    BigDecimal.valueOf(4300),
                    BigDecimal.valueOf(92000),
                    5,
                    false,
                    BigDecimal.valueOf(3000),
                    BigDecimal.valueOf(15000)
            ),
            new Recipe(
                    "PUT-C1-25C",
                    "PuThai 25Kg Cam",
                    "Bao 25kg, keo dán gạch C1 gốc xi măng",
                    C1_CATEGORY,
                    "PTC25",
                    "PKG-PUT-25C",
                    "Vỏ bao Keo PuThai 25Kg Cam",
                    BigDecimal.valueOf(4300),
                    BigDecimal.valueOf(93000),
                    3,
                    false,
                    BigDecimal.valueOf(2000),
                    BigDecimal.valueOf(10000)
            ),
            new Recipe(
                    "MAT-C1-20D",
                    "Mastik Pro 20Kg Đỏ",
                    "Bao 20kg, keo dán gạch C1 gốc xi măng",
                    C1_CATEGORY,
                    "MTD20",
                    "PKG-MAT-20D",
                    "Vỏ bao Keo Mastik Pro 20Kg Đỏ",
                    BigDecimal.valueOf(3900),
                    BigDecimal.valueOf(78000),
                    3,
                    false,
                    BigDecimal.valueOf(2000),
                    BigDecimal.valueOf(10000)
            ),
            new Recipe(
                    "MAT-C1-20X",
                    "Mastik Pro 20Kg Xanh",
                    "Bao 20kg, keo dán gạch C1 gốc xi măng",
                    C1_CATEGORY,
                    "MTX20",
                    "PKG-MAT-20X",
                    "Vỏ bao Keo Mastik Pro 20Kg Xanh",
                    BigDecimal.valueOf(3900),
                    BigDecimal.valueOf(78500),
                    3,
                    false,
                    BigDecimal.valueOf(2000),
                    BigDecimal.valueOf(10000)
            ),
            new Recipe(
                    "MAT-C2-20",
                    "Mastik Pro C2 20Kg",
                    "Bao 20kg, keo dán gạch C2 cường độ cao",
                    C2_CATEGORY,
                    "MTC20",
                    "PKG-MAT-C2-20",
                    "Vỏ bao Keo Mastik Pro C2 20Kg",
                    BigDecimal.valueOf(4100),
                    BigDecimal.valueOf(135000),
                    2,
                    true,
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(8000)
            )
    );

    public static final String PRIMARY_CODE = RECIPES.getFirst().code();

    private final ProductRepository repository;

    private final SampleDataFactory factory;

    public static Optional<Recipe> findRecipe(String code) {

        return RECIPES.stream()
                .filter(recipe -> recipe.code().equals(code))
                .findFirst();

    }

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
