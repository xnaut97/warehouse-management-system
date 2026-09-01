package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MaterialSeeder {

    public record Recipe(
            String code,
            String name,
            String unit,
            BigDecimal unitPrice,
            String supplierCode,
            BigDecimal minimumStock,
            BigDecimal maximumStock,
            BigDecimal receiptLotSize,
            BigDecimal receiptMinimum,
            BigDecimal receiptMaximum,
            int coverDays
    ) {
    }

    public static final String SAND = "MAT-SAND";

    public static final String CEMENT = "MAT-CEMENT";

    public static final String HPMC = "MAT-HPMC";

    public static final String RDP = "MAT-RDP";

    public static final String PACKAGING_PREFIX = "MAT-BAG-";

    public static final String PIECE_UNIT = "Cái";

    public static final List<Recipe> PRODUCTION_RECIPES = List.of(
            new Recipe(
                    SAND,
                    "Cát nghiền",
                    "Kg",
                    BigDecimal.valueOf(480),
                    SupplierSeeder.SAND_CODE,
                    BigDecimal.valueOf(40000),
                    BigDecimal.valueOf(115200),
                    BigDecimal.valueOf(38400),
                    BigDecimal.valueOf(38400),
                    BigDecimal.valueOf(76800),
                    3
            ),
            new Recipe(
                    CEMENT,
                    "Xi măng rời",
                    "Kg",
                    BigDecimal.valueOf(1650),
                    SupplierSeeder.CEMENT_CODE,
                    BigDecimal.valueOf(30000),
                    BigDecimal.valueOf(105000),
                    BigDecimal.TEN,
                    BigDecimal.valueOf(32000),
                    BigDecimal.valueOf(35000),
                    3
            ),
            new Recipe(
                    HPMC,
                    "Phụ gia HPMC",
                    "Kg",
                    BigDecimal.valueOf(118000),
                    SupplierSeeder.ADDITIVE_CODE,
                    BigDecimal.valueOf(500),
                    BigDecimal.valueOf(4000),
                    BigDecimal.valueOf(25),
                    BigDecimal.valueOf(500),
                    BigDecimal.valueOf(2000),
                    12
            ),
            new Recipe(
                    RDP,
                    "Phụ gia RDP",
                    "Kg",
                    BigDecimal.valueOf(54000),
                    SupplierSeeder.ADDITIVE_CODE,
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(9000),
                    BigDecimal.valueOf(20),
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(5000),
                    12
            ),
            new Recipe(
                    packagingCode("TP-C1-25"),
                    "Vỏ bao keo dán gạch C1 25kg",
                    PIECE_UNIT,
                    BigDecimal.valueOf(4200),
                    SupplierSeeder.PACKAGING_CODE,
                    BigDecimal.valueOf(10000),
                    BigDecimal.valueOf(80000),
                    BigDecimal.valueOf(500),
                    BigDecimal.valueOf(10000),
                    BigDecimal.valueOf(30000),
                    20
            ),
            new Recipe(
                    packagingCode("TP-C1-20"),
                    "Vỏ bao keo dán gạch C1 chống trượt 20kg",
                    PIECE_UNIT,
                    BigDecimal.valueOf(3900),
                    SupplierSeeder.PACKAGING_CODE,
                    BigDecimal.valueOf(6000),
                    BigDecimal.valueOf(50000),
                    BigDecimal.valueOf(500),
                    BigDecimal.valueOf(6000),
                    BigDecimal.valueOf(20000),
                    20
            ),
            new Recipe(
                    packagingCode("TP-C2-25"),
                    "Vỏ bao keo dán gạch C2 25kg",
                    PIECE_UNIT,
                    BigDecimal.valueOf(4600),
                    SupplierSeeder.PACKAGING_CODE,
                    BigDecimal.valueOf(3000),
                    BigDecimal.valueOf(25000),
                    BigDecimal.valueOf(500),
                    BigDecimal.valueOf(4000),
                    BigDecimal.valueOf(12000),
                    20
            )
    );

    private static final BigDecimal LEGACY_MINIMUM_STOCK = BigDecimal.valueOf(500);

    private static final BigDecimal LEGACY_MAXIMUM_STOCK = BigDecimal.valueOf(8000);

    private static final List<Recipe> LEGACY_RECIPES = List.of(
            legacy("MAT-C1", "Keo C1", "Kg", 120, "SUP001"),
            legacy("MAT-C2", "Keo C2", "Kg", 135, "SUP001"),
            legacy("MAT-RESIN", "Nhựa nền", "Kg", 88, "SUP003"),
            legacy("MAT-HARDENER", "Chất đóng rắn", "Kg", 210, "SUP004"),
            legacy("MAT-SOLVENT", "Dung môi pha loãng", "Liter", 45, "SUP004"),
            legacy("MAT-PIGMENT", "Bột màu", "Kg", 310, "SUP005")
    );

    public static final List<Recipe> RECIPES =
            java.util.stream.Stream.concat(
                            PRODUCTION_RECIPES.stream(),
                            LEGACY_RECIPES.stream()
                    )
                    .toList();

    private final MaterialRepository repository;

    private final SupplierRepository supplierRepository;

    private final SampleDataFactory factory;

    public static String packagingCode(String productCode) {

        return PACKAGING_PREFIX + productCode.replaceFirst("^TP-", "");

    }

    public static Optional<Recipe> findRecipe(String code) {

        return PRODUCTION_RECIPES.stream()
                .filter(recipe -> recipe.code().equals(code))
                .findFirst();

    }

    @Transactional
    public void seed() {

        for (Recipe recipe : RECIPES) {

            if (repository.existsByCode(recipe.code())) {
                continue;
            }

            Supplier supplier =
                    supplierRepository.findByCode(recipe.supplierCode())
                            .orElseThrow();

            Material material = factory.material(
                    recipe.code(),
                    recipe.name(),
                    recipe.unit(),
                    recipe.unitPrice(),
                    recipe.minimumStock(),
                    recipe.maximumStock(),
                    supplier
            );

            repository.save(material);

        }

        System.out.println("✓ Materials seeded");

    }

    private static Recipe legacy(
            String code,
            String name,
            String unit,
            int unitPrice,
            String supplierCode
    ) {

        return new Recipe(
                code,
                name,
                unit,
                BigDecimal.valueOf(unitPrice),
                supplierCode,
                LEGACY_MINIMUM_STOCK,
                LEGACY_MAXIMUM_STOCK,
                BigDecimal.ONE,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(2000),
                10
        );

    }

}
