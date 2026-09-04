package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.bom.BOMItemRequest;
import com.github.xnaut97.wms.dto.bom.BOMRequest;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.bom.BOMRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.service.bom.BOMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BOMSeeder {

    public record Line(
            String materialCode,
            BigDecimal consumptionQuantity,
            BigDecimal mixingRatio,
            BigDecimal maxWasteRatio
    ) {
    }

    public record Formula(
            String code,
            String productCode,
            List<Line> mix
    ) {
    }

    private static final BigDecimal PACKAGING_QUANTITY = BigDecimal.ONE;

    private static final BigDecimal PACKAGING_RATIO =
            BigDecimal.valueOf(100);

    private static final BigDecimal PACKAGING_WASTE_RATIO =
            new BigDecimal("2.0");

    public static final List<Formula> FORMULAS = List.of(
            new Formula(
                    "BOM-C1-25X",
                    "MAT-C1-25X",
                    standard25()
            ),
            new Formula(
                    "BOM-C1-25V",
                    "MAT-C1-25V",
                    standard25()
            ),
            new Formula(
                    "BOM-PUT-25D",
                    "PUT-C1-25D",
                    standard25()
            ),
            new Formula(
                    "BOM-PUT-25C",
                    "PUT-C1-25C",
                    standard25()
            ),
            new Formula(
                    "BOM-C1-20D",
                    "MAT-C1-20D",
                    standard20()
            ),
            new Formula(
                    "BOM-C1-20X",
                    "MAT-C1-20X",
                    standard20()
            ),
            new Formula(
                    "BOM-C2-20",
                    "MAT-C2-20",
                    List.of(
                            mix(MaterialSeeder.SAND, "12", "60.0", "1.5"),
                            mix(MaterialSeeder.CEMENT, "8", "40.0", "1.0"),
                            mix(MaterialSeeder.RDP, "0.30", "1.5", "0.5"),
                            mix(MaterialSeeder.HPMC, "0.10", "0.5", "0.5")
                    )
            )
    );

    private final BOMRepository repository;

    private final BOMService service;

    private final ProductRepository productRepository;

    private final MaterialRepository materialRepository;

    public static List<Line> allLines(Formula formula) {

        List<Line> lines = new ArrayList<>(formula.mix());

        lines.add(packagingLine(formula.productCode()));

        return lines;

    }

    @Transactional
    public void seed() {

        for (Formula formula : FORMULAS) {

            Optional<Product> product =
                    productRepository.findByCode(formula.productCode());

            if (product.isEmpty() || hasBOM(product.get())) {
                continue;
            }

            List<Line> lines = allLines(formula);

            if (!materialsAvailable(lines)) {
                continue;
            }

            service.create(request(product.get(), formula, lines));

        }

        System.out.println("✓ BOMs seeded");

    }

    private boolean hasBOM(Product product) {

        return repository.findAll().stream()
                .anyMatch(bom -> bom.getProduct().getId()
                        .equals(product.getId()));

    }

    private boolean materialsAvailable(List<Line> lines) {

        return lines.stream()
                .allMatch(line ->
                        materialRepository.existsByCode(line.materialCode()));

    }

    private BOMRequest request(
            Product product,
            Formula formula,
            List<Line> lines
    ) {

        BOMRequest request = new BOMRequest();

        request.setCode(formula.code());

        request.setProductId(product.getId());

        request.setItems(lines.stream().map(this::item).toList());

        return request;

    }

    private BOMItemRequest item(Line line) {

        BOMItemRequest item = new BOMItemRequest();

        item.setMaterialId(
                materialRepository.findByCode(line.materialCode())
                        .orElseThrow()
                        .getId()
        );

        item.setConsumptionQuantity(line.consumptionQuantity());

        item.setMixingRatio(line.mixingRatio());

        item.setMaxWasteRatio(line.maxWasteRatio());

        return item;

    }

    private static List<Line> standard25() {

        return List.of(
                mix(MaterialSeeder.SAND, "15", "60.0", "1.5"),
                mix(MaterialSeeder.CEMENT, "10", "40.0", "1.0"),
                mix(MaterialSeeder.RDP, "0.25", "1.0", "0.5"),
                mix(MaterialSeeder.HPMC, "0.10", "0.4", "0.5")
        );

    }

    private static List<Line> standard20() {

        return List.of(
                mix(MaterialSeeder.SAND, "12", "60.0", "1.5"),
                mix(MaterialSeeder.CEMENT, "8", "40.0", "1.0"),
                mix(MaterialSeeder.RDP, "0.25", "1.25", "0.5"),
                mix(MaterialSeeder.HPMC, "0.10", "0.5", "0.5")
        );

    }

    private static Line packagingLine(String productCode) {

        return new Line(
                MaterialSeeder.packagingCode(productCode),
                PACKAGING_QUANTITY,
                PACKAGING_RATIO,
                PACKAGING_WASTE_RATIO
        );

    }

    private static Line mix(
            String materialCode,
            String consumptionQuantity,
            String mixingRatio,
            String maxWasteRatio
    ) {

        return new Line(
                materialCode,
                new BigDecimal(consumptionQuantity),
                new BigDecimal(mixingRatio),
                new BigDecimal(maxWasteRatio)
        );

    }

}
