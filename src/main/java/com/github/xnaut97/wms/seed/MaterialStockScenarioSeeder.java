package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.receipt.AddReceiptItemRequest;
import com.github.xnaut97.wms.dto.receipt.ReceiptRequest;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.goods.GoodsReceiptItem;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.SupplierRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptItemRepository;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.service.ReceiptService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MaterialStockScenarioSeeder {

    private record StockLine(
            String materialCode,
            String quantity,
            String lotNumber,
            Integer expiresInDays
    ) {
    }

    private static final List<StockLine> MATERIAL_WAREHOUSE_LINES = List.of(
            new StockLine(MaterialSeeder.MATERIAL_C1, "1000", "LOT-C1-2601", 300),
            new StockLine(MaterialSeeder.MATERIAL_C2, "525", "LOT-C2-2601", 210),
            new StockLine(MaterialSeeder.MATERIAL_RESIN, "110", "LOT-RESIN-2601", 120),
            new StockLine(MaterialSeeder.MATERIAL_HARDENER, "20", "LOT-HARDENER-2601", 25),
            new StockLine(MaterialSeeder.MATERIAL_SOLVENT, "0", null, null)
    );

    private static final List<StockLine> PRODUCT_WAREHOUSE_LINES = List.of(
            new StockLine(MaterialSeeder.MATERIAL_C1, "250", "LOT-C1-2602", 260),
            new StockLine(MaterialSeeder.MATERIAL_C2, "60", "LOT-C2-2602", 190)
    );

    private final ReceiptService receiptService;

    private final GoodsReceiptItemRepository receiptItemRepository;

    private final MaterialInventoryRepository materialInventoryRepository;

    private final MaterialRepository materialRepository;

    private final WarehouseRepository warehouseRepository;

    private final SupplierRepository supplierRepository;

    @Transactional
    public void seed() {

        Optional<Supplier> supplier =
                supplierRepository.findByCode("SUP001");

        if (supplier.isEmpty()) return;

        Map<String, List<StockLine>> plan = new LinkedHashMap<>();

        plan.put(
                WarehouseService.MATERIAL_WAREHOUSE_CODE,
                MATERIAL_WAREHOUSE_LINES
        );

        plan.put(
                WarehouseService.PRODUCT_WAREHOUSE_CODE,
                PRODUCT_WAREHOUSE_LINES
        );

        plan.forEach((warehouseCode, lines) ->
                warehouseRepository.findByCode(warehouseCode)
                        .ifPresent(warehouse ->
                                seedWarehouse(
                                        warehouse,
                                        lines,
                                        supplier.get()
                                )
                        )
        );

        System.out.println("✓ Material stock scenario seeded");

    }

    private void seedWarehouse(
            Warehouse warehouse,
            List<StockLine> lines,
            Supplier supplier
    ) {

        List<StockLine> pending = new ArrayList<>();

        for (StockLine line : lines) {

            Optional<Material> material =
                    materialRepository.findByCode(line.materialCode());

            if (material.isEmpty()) continue;

            boolean alreadyStocked = materialInventoryRepository
                    .findByWarehouseIdAndMaterialId(
                            warehouse.getId(),
                            material.get().getId()
                    )
                    .isPresent();

            if (alreadyStocked) continue;

            pending.add(line);

        }

        if (pending.isEmpty()) return;

        List<StockLine> receivable = pending.stream()
                .filter(line -> new BigDecimal(line.quantity())
                        .compareTo(BigDecimal.ZERO) > 0)
                .toList();

        pending.stream()
                .filter(line -> !receivable.contains(line))
                .forEach(line -> createEmptyInventory(warehouse, line));

        if (receivable.isEmpty()) return;

        ReceiptRequest request = new ReceiptRequest();

        request.setSupplierId(supplier.getId());

        request.setWarehouseId(warehouse.getId());

        request.setReceiptDate(LocalDate.now().minusDays(7));

        Long receiptId = receiptService.create(request).getId();

        for (StockLine line : receivable) {

            Material material =
                    materialRepository.findByCode(line.materialCode())
                            .orElseThrow();

            AddReceiptItemRequest item = new AddReceiptItemRequest();

            item.setMaterialId(material.getId());

            item.setQuantity(new BigDecimal(line.quantity()));

            item.setUnitPrice(material.getUnitPrice());

            Long itemId = receiptService.addItem(receiptId, item).getId();

            applyLot(itemId, line);

        }

        receiptService.confirm(receiptId);

    }

    private void applyLot(
            Long itemId,
            StockLine line
    ) {

        if (line.lotNumber() == null) return;

        GoodsReceiptItem item =
                receiptItemRepository.findById(itemId)
                        .orElseThrow();

        item.setLotNumber(line.lotNumber());

        if (line.expiresInDays() != null) {
            item.setExpirationDate(
                    LocalDate.now().plusDays(line.expiresInDays())
            );
        }

        receiptItemRepository.save(item);

    }

    private void createEmptyInventory(
            Warehouse warehouse,
            StockLine line
    ) {

        materialRepository.findByCode(line.materialCode())
                .ifPresent(material -> {

                    MaterialInventory inventory = new MaterialInventory();

                    inventory.setWarehouse(warehouse);

                    inventory.setMaterial(material);

                    inventory.setQuantity(new BigDecimal(line.quantity()));

                    materialInventoryRepository.save(inventory);

                });

    }

}
