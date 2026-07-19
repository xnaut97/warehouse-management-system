package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.receipt.AddReceiptItemRequest;
import com.github.xnaut97.wms.dto.receipt.ReceiptRequest;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.repository.RawMaterialRepository;
import com.github.xnaut97.wms.repository.SupplierRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Transactional
public class ReceiptSeeder {

    private final ReceiptService receiptService;

    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final RawMaterialRepository materialRepository;

    public void seed() {
        if (materialRepository.count() == 0) return;

        List<Supplier> suppliers = supplierRepository.findAll();
        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<RawMaterial> materials = materialRepository.findAll();
        if (materials.isEmpty()) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 20; i++) {

            Supplier supplier = suppliers.get(random.nextInt(suppliers.size()));

            Warehouse warehouse =
                    warehouses.get(random.nextInt(warehouses.size()));

            ReceiptRequest request = new ReceiptRequest();

            request.setSupplierId(supplier.getId());

            request.setWarehouseId(warehouse.getId());

            request.setReceiptDate(
                    LocalDate.now().minusDays(random.nextInt(120))
            );

            Long receiptId = receiptService.create(request).getId();

            Collections.shuffle(materials);

            int itemCount = random.nextInt(1, materials.size() + 1);

            for (int j = 0; j < itemCount; j++) {

                RawMaterial material = materials.get(j);

                AddReceiptItemRequest item =
                        new AddReceiptItemRequest();

                item.setMaterialId(material.getId());

                item.setQuantity(
                        BigDecimal.valueOf(
                                random.nextInt(20, 200)
                        )
                );

                item.setUnitPrice(
                        material.getUnitPrice()
                );

                receiptService.addItem(
                        receiptId,
                        item
                );

            }

            float confirmChance = random.nextFloat();
            if (confirmChance < 0.5f)
                receiptService.confirm(receiptId);

        }

        System.out.println("✓ Receipt Seeder Completed");

    }

}