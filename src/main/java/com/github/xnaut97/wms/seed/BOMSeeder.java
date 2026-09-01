package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.bom.BOMItemRequest;
import com.github.xnaut97.wms.dto.bom.BOMRequest;
import com.github.xnaut97.wms.entity.bom.BOM;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.bom.BOMRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.service.bom.BOMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BOMSeeder {

    public record Line(
            String materialCode,
            BigDecimal consumptionQuantity,
            BigDecimal maxWasteRatio
    ) {
    }

    public record Formula(
            String productCode,
            List<Line> mix
    ) {
    }

    private static final BigDecimal PACKAGING_QUANTITY = BigDecimal.ONE;

    private static final BigDecimal PACKAGING_WASTE_RATIO =
            BigDecimal.valueOf(0.50);

    private static final List<Formula> FORMULAS = List.of(
            new Formula(
                    "TP-C1-25",
                    List.of(
                            mix(MaterialSeeder.SAND, "15", "2.00"),
                            mix(MaterialSeeder.CEMENT, "10", "2.00"),
                            mix(MaterialSeeder.RDP, "0.25", "1.00"),
                            mix(MaterialSeeder.HPMC, "0.10", "1.00")
                    )
            ),
            new Formula(
                    "TP-C1-20",
                    List.of(
                            mix(MaterialSeeder.SAND, "12", "2.00"),
                            mix(MaterialSeeder.CEMENT, "8", "2.00"),
                            mix(MaterialSeeder.RDP, "0.20", "1.00"),
                            mix(MaterialSeeder.HPMC, "0.08", "1.00")
                    )
            ),
            new Formula(
                    "TP-C2-25",
                    List.of(
                            mix(MaterialSeeder.SAND, "13", "2.00"),
                            mix(MaterialSeeder.CEMENT, "12", "2.00"),
                            mix(MaterialSeeder.RDP, "0.50", "1.00"),
                            mix(MaterialSeeder.HPMC, "0.15", "1.00")
                    )
            )
    );

    private final BOMRepository repository;

    private final BOMService service;

    private final ProductRepository productRepository;

    private final MaterialRepository materialRepository;

    @Transactional
    public void seed() {

        for (Formula formula : FORMULAS) {

            Optional<Product> product =
                    productRepository.findByCode(formula.productCode());

            if (product.isEmpty() || hasBOM(product.get())) {
                continue;
            }

            if (!materialsAvailable(formula)) {
                continue;
            }

            service.create(request(product.get(), formula));

        }

        System.out.println("✓ BOMs seeded");

    }

    private boolean hasBOM(Product product) {

        return repository.findAll().stream()
                .anyMatch(bom -> bom.getProduct().getId()
                        .equals(product.getId()));

    }

    private boolean materialsAvailable(Formula formula) {

        if (!materialRepository.existsByCode(
                MaterialSeeder.packagingCode(formula.productCode())
        )) {
            return false;
        }

        return formula.mix().stream()
                .allMatch(line ->
                        materialRepository.existsByCode(line.materialCode()));

    }

    private BOMRequest request(
            Product product,
            Formula formula
    ) {

        BOMRequest request = new BOMRequest();

        request.setCode("BOM-" + product.getCode());

        request.setProductId(product.getId());

        request.setItems(items(formula));

        return request;

    }

    private List<BOMItemRequest> items(Formula formula) {

        BigDecimal total = formula.mix().stream()
                .map(Line::consumptionQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BOMItemRequest> items = new ArrayList<>();

        BigDecimal allocated = BigDecimal.ZERO;

        for (int index = 0; index < formula.mix().size(); index++) {

            Line line = formula.mix().get(index);

            BigDecimal ratio = index == formula.mix().size() - 1
                    ? BigDecimal.valueOf(100).subtract(allocated)
                    : line.consumptionQuantity()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(total, 2, RoundingMode.HALF_UP);

            allocated = allocated.add(ratio);

            items.add(
                    item(
                            line.materialCode(),
                            line.consumptionQuantity(),
                            ratio,
                            line.maxWasteRatio()
                    )
            );

        }

        items.add(
                item(
                        MaterialSeeder.packagingCode(formula.productCode()),
                        PACKAGING_QUANTITY,
                        BigDecimal.ZERO,
                        PACKAGING_WASTE_RATIO
                )
        );

        return items;

    }

    private BOMItemRequest item(
            String materialCode,
            BigDecimal consumptionQuantity,
            BigDecimal mixingRatio,
            BigDecimal maxWasteRatio
    ) {

        BOMItemRequest item = new BOMItemRequest();

        item.setMaterialId(
                materialRepository.findByCode(materialCode)
                        .orElseThrow()
                        .getId()
        );

        item.setConsumptionQuantity(consumptionQuantity);

        item.setMixingRatio(mixingRatio);

        item.setMaxWasteRatio(maxWasteRatio);

        return item;

    }

    private static Line mix(
            String materialCode,
            String consumptionQuantity,
            String maxWasteRatio
    ) {

        return new Line(
                materialCode,
                new BigDecimal(consumptionQuantity),
                new BigDecimal(maxWasteRatio)
        );

    }

}
