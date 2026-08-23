package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class MaterialSeeder {

    public static final String MATERIAL_C1 = "MAT-C1";

    public static final String MATERIAL_C2 = "MAT-C2";

    public static final String MATERIAL_RESIN = "MAT-RESIN";

    public static final String MATERIAL_HARDENER = "MAT-HARDENER";

    public static final String MATERIAL_SOLVENT = "MAT-SOLVENT";

    public static final String MATERIAL_PIGMENT = "MAT-PIGMENT";

    public static final List<String> SCENARIO_MATERIAL_CODES = List.of(
            MATERIAL_C1,
            MATERIAL_C2,
            MATERIAL_RESIN,
            MATERIAL_HARDENER,
            MATERIAL_SOLVENT,
            MATERIAL_PIGMENT
    );

    private final MaterialRepository repository;

    private final SupplierRepository supplierRepository;

    private final SampleDataFactory factory;

    @Transactional
    public void seed() {
        if (supplierRepository.count() == 0) return;

        repository.findAll().forEach(material -> {
            if (material.getMaximumStock() == null) {
                material.setMaximumStock(BigDecimal.ZERO);
                repository.save(material);
            }
        });

        List<Supplier> suppliers = supplierRepository.findAll();

        Supplier s1 = suppliers.get(0);
        Supplier s2 = suppliers.get(1);
        Supplier s3 = suppliers.get(2);
        Supplier s4 = suppliers.get(3);
        Supplier s5 = suppliers.get(4);

        Stream.of(

                        factory.material("RM001", "Cold Rolled Steel", "Kg", new BigDecimal("52.50"), s1),
                        factory.material("RM002", "Copper Wire 2.5mm", "Meter", new BigDecimal("18.30"), s2),
                        factory.material("RM003", "ABS Plastic Resin", "Kg", new BigDecimal("31.20"), s3),
                        factory.material("RM004", "Aluminum Sheet", "Kg", new BigDecimal("74.10"), s1),
                        factory.material("RM005", "Hydraulic Oil", "Liter", new BigDecimal("95.00"), s4),
                        factory.material("RM006", "Lubricant", "Bottle", new BigDecimal("35.00"), s4),
                        factory.material("RM007", "PVC Pipe", "Meter", new BigDecimal("12.50"), s5),
                        factory.material("RM008", "Bearing 6204", "Piece", new BigDecimal("22.80"), s5),
                        factory.material("RM009", "Control PCB", "Piece", new BigDecimal("285.00"), s2),
                        factory.material("RM010", "Hex Bolt M8", "Piece", new BigDecimal("2.50"), s3),

                        factory.material(MATERIAL_C1, "Keo C1", "Kg", new BigDecimal("120.00"), s1),
                        factory.material(MATERIAL_C2, "Keo C2", "Kg", new BigDecimal("135.00"), s1),
                        factory.material(MATERIAL_RESIN, "Nhựa nền", "Kg", new BigDecimal("88.00"), s3),
                        factory.material(MATERIAL_HARDENER, "Chất đóng rắn", "Kg", new BigDecimal("210.00"), s4),
                        factory.material(MATERIAL_SOLVENT, "Dung môi pha loãng", "Liter", new BigDecimal("45.00"), s4),
                        factory.material(MATERIAL_PIGMENT, "Bột màu", "Kg", new BigDecimal("310.00"), s5)

                )
                .filter(m -> !repository.existsByCode(m.getCode()))
                .forEach(repository::save);

        System.out.println("✓ Raw materials seeded");

    }

}
