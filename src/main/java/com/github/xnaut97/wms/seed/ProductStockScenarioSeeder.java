package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.product.receipt.AddProductReceiptItemRequest;
import com.github.xnaut97.wms.dto.product.receipt.ProductReceiptRequest;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.repository.SupplierRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.service.product.ProductReceiptService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductStockScenarioSeeder {

    public static final String ANCHOR_PRODUCT_CODE = "FP001";

    public static final String ANCHOR_LOT_NUMBER = "LOT-FP001-A";

    private record ReceiptLine(
            String productCode,
            String quantity,
            String unitPrice,
            String lotNumber,
            Integer expiresInDays
    ) {
    }

    private static final List<ReceiptLine> MAIN_LINES = List.of(
            new ReceiptLine("FP001", "400", "1850000", ANCHOR_LOT_NUMBER, 240),
            new ReceiptLine("FP002", "220", "2400000", "LOT-FP002-A", 180),
            new ReceiptLine("FP003", "80", "1250000", "LOT-FP003-A", 20),
            new ReceiptLine("FP004", "500", "640000", "LOT-FP004-A", 365),
            new ReceiptLine("FP005", "60", "3150000", null, null)
    );

    private static final List<ReceiptLine> SECOND_LOT_LINES = List.of(
            new ReceiptLine("FP001", "150", "1910000", "LOT-FP001-B", 45)
    );

    private static final List<ReceiptLine> DRAFT_LINES = List.of(
            new ReceiptLine("FP002", "100", "2450000", "LOT-FP002-B", 200)
    );

    private final ProductReceiptService productReceiptService;

    private final ProductInventoryRepository productInventoryRepository;

    private final ProductRepository productRepository;

    private final WarehouseRepository warehouseRepository;

    private final SupplierRepository supplierRepository;

    @Transactional
    public void seed() {

        Optional<Warehouse> warehouse =
                warehouseRepository.findByCode(
                        WarehouseService.PRODUCT_WAREHOUSE_CODE
                );

        if (warehouse.isEmpty()) return;

        Optional<Product> anchor =
                productRepository.findByCode(ANCHOR_PRODUCT_CODE);

        if (anchor.isEmpty()) return;

        boolean alreadySeeded = productInventoryRepository
                .findByWarehouseProductAndLot(
                        warehouse.get().getId(),
                        anchor.get().getId(),
                        ANCHOR_LOT_NUMBER
                )
                .isPresent();

        if (alreadySeeded) return;

        Optional<Supplier> supplier =
                supplierRepository.findByCode("SUP002");

        Long supplierId = supplier.map(Supplier::getId).orElse(null);

        createReceipt(
                warehouse.get().getId(),
                supplierId,
                LocalDate.now().minusDays(20),
                MAIN_LINES,
                true
        );

        createReceipt(
                warehouse.get().getId(),
                supplierId,
                LocalDate.now().minusDays(6),
                SECOND_LOT_LINES,
                true
        );

        createReceipt(
                warehouse.get().getId(),
                supplierId,
                LocalDate.now(),
                DRAFT_LINES,
                false
        );

        System.out.println("✓ Product stock scenario seeded");

    }

    private void createReceipt(
            Long warehouseId,
            Long supplierId,
            LocalDate receiptDate,
            List<ReceiptLine> lines,
            boolean confirm
    ) {

        ProductReceiptRequest request = new ProductReceiptRequest();

        request.setWarehouseId(warehouseId);

        request.setSupplierId(supplierId);

        request.setReceiptDate(receiptDate);

        Long receiptId =
                productReceiptService.create(request).getId();

        int added = 0;

        for (ReceiptLine line : lines) {

            Optional<Product> product =
                    productRepository.findByCode(line.productCode());

            if (product.isEmpty()) continue;

            AddProductReceiptItemRequest item =
                    new AddProductReceiptItemRequest();

            item.setProductId(product.get().getId());

            item.setQuantity(new BigDecimal(line.quantity()));

            item.setUnitPrice(new BigDecimal(line.unitPrice()));

            item.setLotNumber(line.lotNumber());

            if (line.expiresInDays() != null) {
                item.setExpirationDate(
                        LocalDate.now().plusDays(line.expiresInDays())
                );
            }

            productReceiptService.addItem(receiptId, item);

            added++;

        }

        if (confirm && added > 0) {
            productReceiptService.confirm(receiptId);
        }

    }

}
