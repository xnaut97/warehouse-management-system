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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    public static final String SAND = "NVL-SAND";

    public static final String CEMENT = "NVL-CEMENT";

    public static final String HPMC = "NVL-HPMC";

    public static final String RDP = "NVL-RDP";

    public static final String PIECE_UNIT = "Cái";

    public static final BigDecimal PACKAGING_MINIMUM_STOCK =
            BigDecimal.valueOf(2000);

    public static final BigDecimal PACKAGING_MAXIMUM_STOCK =
            BigDecimal.valueOf(15000);

    public static final BigDecimal SAND_CONTAINER =
            BigDecimal.valueOf(38400);

    public static final BigDecimal CEMENT_RECEIPT_MINIMUM =
            BigDecimal.valueOf(32000);

    public static final BigDecimal CEMENT_RECEIPT_MAXIMUM =
            BigDecimal.valueOf(35000);

    public static final BigDecimal HPMC_LOT = BigDecimal.valueOf(25);

    public static final BigDecimal RDP_LOT = BigDecimal.valueOf(20);

    public static final BigDecimal PACKAGING_LOT = BigDecimal.valueOf(500);

    private static final List<Recipe> BULK_RECIPES = List.of(
            new Recipe(
                    SAND,
                    "Cát sấy",
                    "Kg",
                    BigDecimal.valueOf(480),
                    SupplierSeeder.SAND_CODE,
                    BigDecimal.valueOf(18000),
                    BigDecimal.valueOf(76800),
                    SAND_CONTAINER,
                    SAND_CONTAINER,
                    SAND_CONTAINER,
                    2
            ),
            new Recipe(
                    CEMENT,
                    "Xi măng rời",
                    "Kg",
                    BigDecimal.valueOf(1650),
                    SupplierSeeder.CEMENT_CODE,
                    BigDecimal.valueOf(5000),
                    BigDecimal.valueOf(35000),
                    BigDecimal.valueOf(0.5),
                    CEMENT_RECEIPT_MINIMUM,
                    CEMENT_RECEIPT_MAXIMUM,
                    0
            ),
            new Recipe(
                    HPMC,
                    "Phụ gia HPMC",
                    "Kg",
                    BigDecimal.valueOf(118000),
                    SupplierSeeder.ADDITIVE_CODE,
                    BigDecimal.valueOf(100),
                    BigDecimal.valueOf(5000),
                    HPMC_LOT,
                    BigDecimal.valueOf(500),
                    BigDecimal.valueOf(2500),
                    12
            ),
            new Recipe(
                    RDP,
                    "Phụ gia RDP",
                    "Kg",
                    BigDecimal.valueOf(54000),
                    SupplierSeeder.ADDITIVE_CODE,
                    BigDecimal.valueOf(500),
                    BigDecimal.valueOf(10000),
                    RDP_LOT,
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(4000),
                    12
            )
    );

    public static final List<Recipe> PACKAGING_RECIPES =
            ProductSeeder.RECIPES.stream()
                    .map(MaterialSeeder::packaging)
                    .toList();

    public static final List<Recipe> PRODUCTION_RECIPES =
            Stream.concat(
                            BULK_RECIPES.stream(),
                            PACKAGING_RECIPES.stream()
                    )
                    .toList();

    private static final BigDecimal LEGACY_MINIMUM_STOCK =
            BigDecimal.valueOf(500);

    private static final BigDecimal LEGACY_MAXIMUM_STOCK =
            BigDecimal.valueOf(8000);

    private static final List<Recipe> LEGACY_RECIPES = List.of(
            legacy("NVL-RESIN", "Nhựa nền", "Kg", 88, "SUP003"),
            legacy("NVL-HARDENER", "Chất đóng rắn", "Kg", 210, "SUP004"),
            legacy("NVL-SOLVENT", "Dung môi pha loãng", "Liter", 45, "SUP004"),
            legacy("NVL-PIGMENT", "Bột màu", "Kg", 310, "SUP005")
    );

    public static final List<Recipe> RECIPES =
            Stream.concat(
                            PRODUCTION_RECIPES.stream(),
                            LEGACY_RECIPES.stream()
                    )
                    .toList();

    private final MaterialRepository repository;

    private final SupplierRepository supplierRepository;

    private final SampleDataFactory factory;

    public static String packagingCode(String productCode) {

        return ProductSeeder.findRecipe(productCode)
                .map(ProductSeeder.Recipe::packagingCode)
                .orElseThrow();

    }

    public static boolean isPackaging(String materialCode) {

        return PACKAGING_RECIPES.stream()
                .anyMatch(recipe -> recipe.code().equals(materialCode));

    }

    public static Optional<Recipe> findRecipe(String code) {

        return PRODUCTION_RECIPES.stream()
                .filter(recipe -> recipe.code().equals(code))
                .findFirst();

    }

    @Transactional
    public void seed() {

        List<Material> pending = new ArrayList<>();

        for (Recipe recipe : RECIPES) {

            if (repository.existsByCode(recipe.code())) {
                continue;
            }

            Supplier supplier =
                    supplierRepository.findByCode(recipe.supplierCode())
                            .orElseThrow();

            pending.add(
                    factory.material(
                            recipe.code(),
                            recipe.name(),
                            recipe.unit(),
                            recipe.unitPrice(),
                            recipe.minimumStock(),
                            recipe.maximumStock(),
                            supplier
                    )
            );

        }

        repository.saveAll(pending);

        System.out.println("✓ Materials seeded");

    }

    private static Recipe packaging(ProductSeeder.Recipe product) {

        return new Recipe(
                product.packagingCode(),
                product.packagingName(),
                PIECE_UNIT,
                product.packagingPrice(),
                SupplierSeeder.PACKAGING_CODE,
                PACKAGING_MINIMUM_STOCK,
                PACKAGING_MAXIMUM_STOCK,
                PACKAGING_LOT,
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(4000),
                6
        );

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
