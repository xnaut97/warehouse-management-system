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

@Component
@RequiredArgsConstructor
public class MaterialSeeder {

    public record Recipe(
            String code,
            String name,
            String unit,
            BigDecimal unitPrice,
            String supplierCode
    ) {
    }

    public static final List<Recipe> RECIPES = List.of(
            new Recipe("MAT-C1", "Keo C1", "Kg", BigDecimal.valueOf(120), "SUP001"),
            new Recipe("MAT-C2", "Keo C2", "Kg", BigDecimal.valueOf(135), "SUP001"),
            new Recipe("MAT-RESIN", "Nhựa nền", "Kg", BigDecimal.valueOf(88), "SUP003"),
            new Recipe("MAT-HARDENER", "Chất đóng rắn", "Kg", BigDecimal.valueOf(210), "SUP004"),
            new Recipe("MAT-SOLVENT", "Dung môi pha loãng", "Liter", BigDecimal.valueOf(45), "SUP004"),
            new Recipe("MAT-PIGMENT", "Bột màu", "Kg", BigDecimal.valueOf(310), "SUP005")
    );

    private static final BigDecimal MINIMUM_STOCK = BigDecimal.valueOf(500);

    private static final BigDecimal MAXIMUM_STOCK = BigDecimal.valueOf(8000);

    private final MaterialRepository repository;

    private final SupplierRepository supplierRepository;

    private final SampleDataFactory factory;

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
                    MINIMUM_STOCK,
                    MAXIMUM_STOCK,
                    supplier
            );

            repository.save(material);

        }

        System.out.println("✓ Materials seeded");

    }

}
