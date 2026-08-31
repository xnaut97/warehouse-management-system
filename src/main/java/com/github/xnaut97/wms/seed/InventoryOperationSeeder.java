package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.entity.goods.GoodsIssueItem;
import com.github.xnaut97.wms.entity.goods.GoodsReceipt;
import com.github.xnaut97.wms.entity.goods.GoodsReceiptItem;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.entity.product.ProductIssue;
import com.github.xnaut97.wms.entity.product.ProductIssueItem;
import com.github.xnaut97.wms.entity.product.ProductReceipt;
import com.github.xnaut97.wms.entity.product.ProductReceiptItem;
import com.github.xnaut97.wms.entity.stock.Stocktaking;
import com.github.xnaut97.wms.entity.stock.StocktakingItem;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import com.github.xnaut97.wms.enums.StocktakingType;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.CustomerRepository;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.SupplierRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.product.ProductIssueItemRepository;
import com.github.xnaut97.wms.repository.product.ProductIssueRepository;
import com.github.xnaut97.wms.repository.product.ProductReceiptItemRepository;
import com.github.xnaut97.wms.repository.product.ProductReceiptRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingItemBatchRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingItemRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import com.github.xnaut97.wms.service.DocumentNumberService;
import com.github.xnaut97.wms.service.product.ProductService;
import com.github.xnaut97.wms.service.warehouse.MaterialService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InventoryOperationSeeder {

    private static final int WINDOW_DAYS = 45;

    private static final long RANDOM_SEED = 20260901L;

    private static final int[] LARGE_SHIPMENTS = {1200, 1440, 1600};

    private static final DateTimeFormatter LOT_FORMATTER =
            DateTimeFormatter.ofPattern("yyMM");

    private static final List<String> MATERIAL_VARIANCE_REASONS = List.of(
            "Hao hụt trong quá trình bảo quản",
            "Ghi nhận thiếu phiếu xuất sản xuất",
            "Cân lại thực tế sau khi đảo kho",
            "Nhập nhầm đơn vị tính khi ghi sổ"
    );

    private static final List<String> PRODUCT_VARIANCE_REASONS = List.of(
            "Bao rách trong quá trình bốc xếp",
            "Đếm thiếu ở ca trước",
            "Xuất mẫu cho khách chưa lập phiếu",
            "Sai lệch khi ghi nhận lô sản xuất"
    );

    private final WarehouseRepository warehouseRepository;

    private final UserRepository userRepository;

    private final MaterialRepository materialRepository;

    private final ProductRepository productRepository;

    private final SupplierRepository supplierRepository;

    private final CustomerRepository customerRepository;

    private final MaterialInventoryRepository materialInventoryRepository;

    private final ProductInventoryRepository productInventoryRepository;

    private final InventoryTransactionRepository transactionRepository;

    private final GoodsReceiptRepository goodsReceiptRepository;

    private final GoodsReceiptItemRepository goodsReceiptItemRepository;

    private final GoodsIssueRepository goodsIssueRepository;

    private final GoodsIssueItemRepository goodsIssueItemRepository;

    private final ProductReceiptRepository productReceiptRepository;

    private final ProductReceiptItemRepository productReceiptItemRepository;

    private final ProductIssueRepository productIssueRepository;

    private final ProductIssueItemRepository productIssueItemRepository;

    private final StocktakingRepository stocktakingRepository;

    private final StocktakingItemRepository stocktakingItemRepository;

    private final StocktakingItemBatchRepository stocktakingItemBatchRepository;

    private final DocumentNumberService documentNumberService;

    private final MaterialService materialService;

    private final ProductService productService;

    private final SampleDataFactory factory;

    @PersistenceContext
    private EntityManager entityManager;

    private Random random;

    private Warehouse materialWarehouse;

    private Warehouse productWarehouse;

    private List<User> operators;

    private List<Material> materials;

    private List<Supplier> materialSuppliers;

    private List<Product> products;

    private List<Customer> customers;

    private Map<String, BigDecimal> materialPrices;

    private Map<Long, MaterialInventory> materialStock;

    private Map<String, ProductInventory> productLots;

    private Map<LocalDateTime, List<Long>> transactionTimestamps;

    private Set<Long> touchedMaterials;

    private Set<Long> touchedProducts;

    private int operatorCursor;

    private int supplierCursor;

    private int customerCursor;

    private int largeShipmentCursor;

    @Transactional
    public void seed() {

        Product primary =
                productRepository.findByCode(ProductSeeder.PRIMARY_CODE)
                        .orElse(null);

        if (primary == null) {
            return;
        }

        materialWarehouse = warehouseRepository
                .findByCode(WarehouseService.MATERIAL_WAREHOUSE_CODE)
                .orElseThrow();

        productWarehouse = warehouseRepository
                .findByCode(WarehouseService.PRODUCT_WAREHOUSE_CODE)
                .orElseThrow();

        boolean alreadySeeded = !productInventoryRepository
                .findAllByWarehouseIdAndProductId(
                        productWarehouse.getId(),
                        primary.getId()
                )
                .isEmpty();

        if (alreadySeeded) {

            System.out.println("✓ Inventory operations already seeded");

            return;
        }

        prepare();

        LocalDate today = LocalDate.now();

        LocalDate start = today.minusDays(WINDOW_DAYS - 1L);

        LocalDate openDocumentDate = lastWorkingDay(today);

        seedOpeningBalance(start.minusDays(1));

        LocalDate materialCountOne = start.plusDays(13);

        LocalDate productCountOne = start.plusDays(20);

        LocalDate materialCountTwo = start.plusDays(38);

        LocalDate productCountTwo = start.plusDays(43);

        int daysToLargeShipment = 5;

        for (LocalDate date = start;
             !date.isAfter(today);
             date = date.plusDays(1)) {

            if (date.equals(materialCountOne)) {

                seedMaterialStocktaking(
                        date,
                        StocktakingType.PERIODIC,
                        StocktakingStatus.STOCK_BALANCED,
                        "Kiểm kê định kỳ kho nguyên vật liệu"
                );

            }

            if (date.equals(productCountOne)) {

                seedProductStocktaking(
                        date,
                        StocktakingType.PERIODIC,
                        StocktakingStatus.STOCK_BALANCED,
                        "Kiểm kê định kỳ kho thành phẩm"
                );

            }

            if (date.equals(materialCountTwo)) {

                seedMaterialStocktaking(
                        date,
                        StocktakingType.AD_HOC,
                        StocktakingStatus.COUNT_CONFIRMED,
                        "Kiểm kê đột xuất kho nguyên vật liệu, chờ cân bằng tồn"
                );

            }

            if (date.equals(productCountTwo)) {

                seedProductStocktaking(
                        date,
                        StocktakingType.PERIODIC,
                        StocktakingStatus.IN_PROGRESS,
                        "Kiểm kê cuối kỳ kho thành phẩm, đang đếm thực tế"
                );

            }

            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {

                daysToLargeShipment--;

                continue;
            }

            DayOfWeek day = date.getDayOfWeek();

            if (day == DayOfWeek.MONDAY || day == DayOfWeek.THURSDAY) {
                seedMaterialReceipt(date);
            }

            if (day == DayOfWeek.MONDAY
                    || day == DayOfWeek.WEDNESDAY
                    || day == DayOfWeek.FRIDAY) {

                seedMaterialIssue(date);

            }

            seedProductReceipt(date, !date.equals(openDocumentDate));

            daysToLargeShipment--;

            int shipped;

            if (daysToLargeShipment <= 0) {

                shipped = LARGE_SHIPMENTS[
                        largeShipmentCursor++ % LARGE_SHIPMENTS.length
                        ];

                daysToLargeShipment = 8 + random.nextInt(7);

            } else if (random.nextInt(100) < 18) {

                shipped = 0;

            } else {

                shipped = 200 + random.nextInt(201);

            }

            if (shipped > 0) {

                seedProductIssue(
                        date,
                        shipped,
                        !date.equals(openDocumentDate)
                );

            }

        }

        entityManager.flush();

        applyTransactionTimestamps();

        touchedMaterials.forEach(materialService::recalculateAveragePrice);

        touchedProducts.forEach(productService::recalculateAveragePrice);

        System.out.println("✓ Inventory operations seeded");

    }

    private void prepare() {

        random = new Random(RANDOM_SEED);

        operators = List.of(
                userRepository.findByUsername("staff").orElseThrow(),
                userRepository.findByUsername("manager").orElseThrow(),
                userRepository.findByUsername("admin").orElseThrow()
        );

        materials = MaterialSeeder.RECIPES.stream()
                .map(recipe -> materialRepository.findByCode(recipe.code())
                        .orElseThrow())
                .toList();

        materialPrices = new LinkedHashMap<>();

        MaterialSeeder.RECIPES.forEach(recipe ->
                materialPrices.put(recipe.code(), recipe.unitPrice()));

        materialSuppliers = MaterialSeeder.RECIPES.stream()
                .map(MaterialSeeder.Recipe::supplierCode)
                .distinct()
                .map(code -> supplierRepository.findByCode(code).orElseThrow())
                .toList();

        products = ProductSeeder.RECIPES.stream()
                .map(recipe -> productRepository.findByCode(recipe.code())
                        .orElseThrow())
                .toList();

        customers = CustomerSeeder.CODES.stream()
                .map(code -> customerRepository.findByCode(code).orElseThrow())
                .toList();

        materialStock = new LinkedHashMap<>();

        productLots = new LinkedHashMap<>();

        transactionTimestamps = new LinkedHashMap<>();

        touchedMaterials = new LinkedHashSet<>();

        touchedProducts = new LinkedHashSet<>();

    }

    private void seedOpeningBalance(LocalDate date) {

        GoodsReceipt materialOpening = goodsReceiptRepository.save(
                factory.goodsReceipt(
                        documentNumberService.next(
                                DocumentType.GOODS_RECEIPT,
                                date
                        ),
                        materialSuppliers.getFirst(),
                        materialWarehouse,
                        date,
                        ReceiptStatus.CONFIRMED,
                        nextOperator()
                )
        );

        BigDecimal materialTotal = BigDecimal.ZERO;

        int[] openingMaterialQuantities = {3200, 2400, 2800, 1600, 1900, 900};

        for (int index = 0; index < materials.size(); index++) {

            Material material = materials.get(index);

            BigDecimal quantity =
                    BigDecimal.valueOf(openingMaterialQuantities[index]);

            GoodsReceiptItem item = goodsReceiptItemRepository.save(
                    factory.goodsReceiptItem(
                            materialOpening,
                            material,
                            quantity,
                            unitPriceOf(material)
                    )
            );

            materialTotal = materialTotal.add(item.getAmount());

            increaseMaterialStock(material, quantity);

            recordTransaction(
                    material,
                    InventoryTransactionType.IN,
                    quantity,
                    materialOpening.getReceiptNo(),
                    materialOpening.getCreatedBy(),
                    date.atTime(8, 0)
            );

        }

        materialOpening.setTotalAmount(materialTotal);

        goodsReceiptRepository.save(materialOpening);

        ProductReceipt productOpening = productReceiptRepository.save(
                factory.productReceipt(
                        documentNumberService.next(
                                DocumentType.GOODS_RECEIPT,
                                date
                        ),
                        productWarehouse,
                        date,
                        ReceiptStatus.CONFIRMED,
                        nextOperator()
                )
        );

        BigDecimal productTotal = BigDecimal.ZERO;

        int[] openingProductQuantities = {900, 640, 420};

        for (int index = 0; index < products.size(); index++) {

            Product product = products.get(index);

            ProductInventory lot = resolveLot(product, date);

            BigDecimal quantity =
                    BigDecimal.valueOf(openingProductQuantities[index]);

            ProductReceiptItem item = productReceiptItemRepository.save(
                    factory.productReceiptItem(
                            productOpening,
                            product,
                            quantity,
                            ProductSeeder.RECIPES.get(index).costPrice(),
                            lot.getLotNumber(),
                            lot.getExpirationDate()
                    )
            );

            productTotal = productTotal.add(item.getAmount());

            lot.setQuantity(lot.getQuantity().add(quantity));

            productInventoryRepository.save(lot);

        }

        productOpening.setTotalAmount(productTotal);

        productReceiptRepository.save(productOpening);

    }

    private void seedMaterialReceipt(LocalDate date) {

        Supplier supplier =
                materialSuppliers.get(supplierCursor++ % materialSuppliers.size());

        List<Material> supplied = suppliedMaterials(supplier);

        if (supplied.isEmpty()) {
            return;
        }

        ReceiptStatus status = random.nextInt(100) < 8
                ? ReceiptStatus.CANCELLED
                : ReceiptStatus.CONFIRMED;

        GoodsReceipt receipt = goodsReceiptRepository.save(
                factory.goodsReceipt(
                        documentNumberService.next(
                                DocumentType.GOODS_RECEIPT,
                                date
                        ),
                        supplier,
                        materialWarehouse,
                        date,
                        status,
                        nextOperator()
                )
        );

        BigDecimal total = BigDecimal.ZERO;

        for (Material material : supplied) {

            BigDecimal quantity =
                    BigDecimal.valueOf(50L * (30 + random.nextInt(41)));

            GoodsReceiptItem item = goodsReceiptItemRepository.save(
                    factory.goodsReceiptItem(
                            receipt,
                            material,
                            quantity,
                            unitPriceOf(material)
                    )
            );

            total = total.add(item.getAmount());

            if (status != ReceiptStatus.CONFIRMED) {
                continue;
            }

            increaseMaterialStock(material, quantity);

            recordTransaction(
                    material,
                    InventoryTransactionType.IN,
                    quantity,
                    receipt.getReceiptNo(),
                    receipt.getCreatedBy(),
                    date.atTime(9, 15)
            );

        }

        receipt.setTotalAmount(total);

        goodsReceiptRepository.save(receipt);

    }

    private void seedMaterialIssue(LocalDate date) {

        List<Material> consumable = new ArrayList<>(materials.stream()
                .filter(material -> stockOf(material)
                        .compareTo(BigDecimal.valueOf(300)) > 0)
                .toList());

        if (consumable.isEmpty()) {
            return;
        }

        Collections.shuffle(consumable, random);

        List<Material> selected = consumable.subList(
                0,
                Math.min(consumable.size(), 3 + random.nextInt(2))
        );

        IssueStatus status = random.nextInt(100) < 10
                ? IssueStatus.DRAFT
                : IssueStatus.CONFIRMED;

        GoodsIssue issue = goodsIssueRepository.save(
                factory.goodsIssue(
                        documentNumberService.next(
                                DocumentType.GOODS_ISSUE,
                                date
                        ),
                        materialWarehouse,
                        null,
                        date,
                        status,
                        nextOperator()
                )
        );

        BigDecimal total = BigDecimal.ZERO;

        for (Material material : selected) {

            BigDecimal available = stockOf(material);

            BigDecimal requested =
                    BigDecimal.valueOf(50L * (8 + random.nextInt(13)));

            BigDecimal quantity = requested.min(
                    available.multiply(BigDecimal.valueOf(0.4))
                            .setScale(0, RoundingMode.DOWN)
            );

            if (quantity.compareTo(BigDecimal.valueOf(50)) < 0) {
                continue;
            }

            GoodsIssueItem item = goodsIssueItemRepository.save(
                    factory.goodsIssueItem(
                            issue,
                            material,
                            quantity,
                            materialPrices.get(material.getCode())
                    )
            );

            total = total.add(item.getAmount());

            if (status != IssueStatus.CONFIRMED) {
                continue;
            }

            decreaseMaterialStock(material, quantity);

            recordTransaction(
                    material,
                    InventoryTransactionType.OUT,
                    quantity,
                    issue.getIssueNo(),
                    issue.getCreatedBy(),
                    date.atTime(16, 30)
            );

        }

        issue.setTotalAmount(total);

        goodsIssueRepository.save(issue);

    }

    private void seedProductReceipt(LocalDate date, boolean confirmed) {

        int produced = random.nextInt(100) < 85
                ? 700 + random.nextInt(201)
                : 250 + random.nextInt(751);

        List<Integer> lines = pickProductLines(
                random.nextInt(100) < 60 ? 3 : 2
        );

        int[] weights = lines.stream()
                .mapToInt(index -> ProductSeeder.RECIPES.get(index).outputWeight())
                .toArray();

        int[] quantities = split(produced, weights);

        ProductReceipt receipt = productReceiptRepository.save(
                factory.productReceipt(
                        documentNumberService.next(
                                DocumentType.GOODS_RECEIPT,
                                date
                        ),
                        productWarehouse,
                        date,
                        confirmed
                                ? ReceiptStatus.CONFIRMED
                                : ReceiptStatus.DRAFT,
                        nextOperator()
                )
        );

        BigDecimal total = BigDecimal.ZERO;

        for (int position = 0; position < lines.size(); position++) {

            int index = lines.get(position);

            Product product = products.get(index);

            ProductInventory lot = resolveLot(product, date);

            BigDecimal quantity = BigDecimal.valueOf(quantities[position]);

            ProductReceiptItem item = productReceiptItemRepository.save(
                    factory.productReceiptItem(
                            receipt,
                            product,
                            quantity,
                            ProductSeeder.RECIPES.get(index).costPrice(),
                            lot.getLotNumber(),
                            lot.getExpirationDate()
                    )
            );

            total = total.add(item.getAmount());

            if (!confirmed) {
                continue;
            }

            lot.setQuantity(lot.getQuantity().add(quantity));

            productInventoryRepository.save(lot);

        }

        receipt.setTotalAmount(total);

        productReceiptRepository.save(receipt);

    }

    private void seedProductIssue(
            LocalDate date,
            int requested,
            boolean confirmed
    ) {

        List<Integer> candidates = new ArrayList<>();

        for (int index = 0; index < products.size(); index++) {

            if (availableOf(products.get(index)).signum() > 0) {
                candidates.add(index);
            }

        }

        if (candidates.isEmpty()) {
            return;
        }

        Collections.shuffle(candidates, random);

        List<Integer> lines = candidates.subList(
                0,
                Math.min(
                        candidates.size(),
                        random.nextInt(100) < 55 ? 1 : 2
                )
        );

        List<Object[]> allocations = new ArrayList<>();

        int remaining = requested;

        for (int position = 0; position < candidates.size(); position++) {

            if (remaining <= 0) {
                break;
            }

            int index = candidates.get(position);

            Product product = products.get(index);

            int available = availableOf(product).intValue();

            int share = position >= lines.size() - 1
                    ? remaining
                    : Math.max(50, roundToTen(requested / lines.size()));

            int take = Math.min(Math.min(share, remaining), available);

            if (take <= 0) {
                continue;
            }

            for (ProductInventory lot : lotsOf(product)) {

                if (take <= 0) {
                    break;
                }

                int fromLot = Math.min(take, lot.getQuantity().intValue());

                if (fromLot <= 0) {
                    continue;
                }

                allocations.add(new Object[]{
                        index,
                        lot,
                        BigDecimal.valueOf(fromLot)
                });

                take -= fromLot;

                remaining -= fromLot;

            }

        }

        if (allocations.isEmpty()) {
            return;
        }

        Customer customer = customers.get(customerCursor++ % customers.size());

        ProductIssue issue = productIssueRepository.save(
                factory.productIssue(
                        documentNumberService.next(
                                DocumentType.GOODS_ISSUE,
                                date
                        ),
                        productWarehouse,
                        customer,
                        date,
                        confirmed
                                ? IssueStatus.CONFIRMED
                                : IssueStatus.DRAFT,
                        nextOperator()
                )
        );

        BigDecimal total = BigDecimal.ZERO;

        for (Object[] allocation : allocations) {

            int index = (Integer) allocation[0];

            ProductInventory lot = (ProductInventory) allocation[1];

            BigDecimal quantity = (BigDecimal) allocation[2];

            ProductIssueItem item = productIssueItemRepository.save(
                    factory.productIssueItem(
                            issue,
                            products.get(index),
                            quantity,
                            ProductSeeder.RECIPES.get(index).salePrice(),
                            lot.getLotNumber(),
                            lot.getExpirationDate()
                    )
            );

            total = total.add(item.getAmount());

            touchedProducts.add(products.get(index).getId());

            if (!confirmed) {
                continue;
            }

            lot.setQuantity(lot.getQuantity().subtract(quantity));

            productInventoryRepository.save(lot);

        }

        issue.setTotalAmount(total);

        productIssueRepository.save(issue);

    }

    private void seedMaterialStocktaking(
            LocalDate date,
            StocktakingType type,
            StocktakingStatus status,
            String note
    ) {

        List<MaterialInventory> inventories = materialInventoryRepository
                .findAllByWarehouseIdOrderByMaterialCodeAsc(
                        materialWarehouse.getId()
                );

        if (inventories.isEmpty()) {
            return;
        }

        Stocktaking stocktaking = stocktakingRepository.save(
                factory.stocktaking(
                        documentNumberService.next(
                                DocumentType.INVENTORY_CHECK,
                                date
                        ),
                        materialWarehouse,
                        date,
                        type,
                        status,
                        note,
                        operators.getFirst(),
                        operators.get(1)
                )
        );

        for (MaterialInventory inventory : inventories) {

            BigDecimal system = inventory.getQuantity();

            BigDecimal variance = status == StocktakingStatus.IN_PROGRESS
                    ? BigDecimal.ZERO
                    : materialVariance(system);

            BigDecimal physical = status == StocktakingStatus.IN_PROGRESS
                    ? null
                    : system.add(variance);

            String reason = variance.signum() == 0
                    ? null
                    : MATERIAL_VARIANCE_REASONS.get(
                    random.nextInt(MATERIAL_VARIANCE_REASONS.size())
            );

            stocktakingItemRepository.save(
                    factory.stocktakingItem(
                            stocktaking,
                            StockGroup.MATERIAL,
                            inventory.getMaterial(),
                            null,
                            system,
                            physical,
                            reason
                    )
            );

            if (status != StocktakingStatus.STOCK_BALANCED
                    || variance.signum() == 0) {
                continue;
            }

            inventory.setQuantity(physical);

            materialInventoryRepository.save(inventory);

            materialStock.put(
                    inventory.getMaterial().getId(),
                    inventory
            );

            recordTransaction(
                    inventory.getMaterial(),
                    InventoryTransactionType.ADJUSTMENT,
                    variance.abs(),
                    stocktaking.getStocktakingNo(),
                    stocktaking.getCreatedBy(),
                    date.atTime(11, 0)
            );

        }

    }

    private void seedProductStocktaking(
            LocalDate date,
            StocktakingType type,
            StocktakingStatus status,
            String note
    ) {

        List<ProductInventory> inventories = productInventoryRepository
                .findAllByWarehouseIdOrderByProductCodeAscLotNumberAsc(
                        productWarehouse.getId()
                );

        if (inventories.isEmpty()) {
            return;
        }

        Map<Long, List<ProductInventory>> grouped = new LinkedHashMap<>();

        for (ProductInventory inventory : inventories) {

            grouped.computeIfAbsent(
                    inventory.getProduct().getId(),
                    key -> new ArrayList<>()
            ).add(inventory);

        }

        Map<Long, BigDecimal> variances = new LinkedHashMap<>();

        Map<Long, String> lotReasons = new LinkedHashMap<>();

        if (status != StocktakingStatus.IN_PROGRESS) {

            List<ProductInventory> shuffled = new ArrayList<>(inventories);

            Collections.shuffle(shuffled, random);

            int counted = Math.min(
                    shuffled.size(),
                    2 + random.nextInt(3)
            );

            for (int index = 0; index < counted; index++) {

                ProductInventory inventory = shuffled.get(index);

                BigDecimal variance =
                        productVariance(inventory.getQuantity());

                if (variance.signum() == 0) {
                    continue;
                }

                variances.put(inventory.getId(), variance);

                lotReasons.put(
                        inventory.getId(),
                        PRODUCT_VARIANCE_REASONS.get(
                                random.nextInt(PRODUCT_VARIANCE_REASONS.size())
                        )
                );

            }

        }

        Stocktaking stocktaking = stocktakingRepository.save(
                factory.stocktaking(
                        documentNumberService.next(
                                DocumentType.INVENTORY_CHECK,
                                date
                        ),
                        productWarehouse,
                        date,
                        type,
                        status,
                        note,
                        operators.getFirst(),
                        operators.get(1)
                )
        );

        for (List<ProductInventory> group : grouped.values()) {

            BigDecimal systemTotal = BigDecimal.ZERO;

            BigDecimal physicalTotal = BigDecimal.ZERO;

            List<BigDecimal> counted = new ArrayList<>();

            List<String> reasons = new ArrayList<>();

            for (ProductInventory inventory : group) {

                BigDecimal system = inventory.getQuantity();

                systemTotal = systemTotal.add(system);

                if (status == StocktakingStatus.IN_PROGRESS) {

                    counted.add(null);

                    reasons.add(null);

                    continue;
                }

                BigDecimal variance = variances.getOrDefault(
                        inventory.getId(),
                        BigDecimal.ZERO
                );

                BigDecimal physical = system.add(variance);

                counted.add(physical);

                reasons.add(lotReasons.get(inventory.getId()));

                physicalTotal = physicalTotal.add(physical);

            }

            StocktakingItem item = stocktakingItemRepository.save(
                    factory.stocktakingItem(
                            stocktaking,
                            StockGroup.PRODUCT,
                            null,
                            group.getFirst().getProduct(),
                            systemTotal,
                            status == StocktakingStatus.IN_PROGRESS
                                    ? null
                                    : physicalTotal,
                            null
                    )
            );

            for (int index = 0; index < group.size(); index++) {

                ProductInventory inventory = group.get(index);

                stocktakingItemBatchRepository.save(
                        factory.stocktakingItemBatch(
                                item,
                                inventory,
                                inventory.getQuantity(),
                                counted.get(index),
                                reasons.get(index)
                        )
                );

                if (status != StocktakingStatus.STOCK_BALANCED) {
                    continue;
                }

                inventory.setQuantity(counted.get(index));

                productInventoryRepository.save(inventory);

            }

        }

    }

    private List<Material> suppliedMaterials(Supplier supplier) {

        List<String> codes = MaterialSeeder.RECIPES.stream()
                .filter(recipe -> recipe.supplierCode()
                        .equals(supplier.getCode()))
                .map(MaterialSeeder.Recipe::code)
                .toList();

        return materials.stream()
                .filter(material -> codes.contains(material.getCode()))
                .toList();
    }

    private ProductInventory resolveLot(Product product, LocalDate date) {

        String lotNumber = "LOT-%s-%s".formatted(
                product.getCode(),
                date.format(LOT_FORMATTER)
        );

        return productLots.computeIfAbsent(lotNumber, key ->
                productInventoryRepository.save(
                        factory.productInventory(
                                productWarehouse,
                                product,
                                key,
                                date.withDayOfMonth(1).plusMonths(12)
                        )
                )
        );
    }

    private List<ProductInventory> lotsOf(Product product) {

        return productLots.values().stream()
                .filter(lot -> lot.getProduct().getId()
                        .equals(product.getId()))
                .filter(lot -> lot.getQuantity().signum() > 0)
                .sorted(Comparator
                        .comparing(ProductInventory::getExpirationDate)
                        .thenComparing(ProductInventory::getLotNumber))
                .toList();
    }

    private BigDecimal availableOf(Product product) {

        return productLots.values().stream()
                .filter(lot -> lot.getProduct().getId()
                        .equals(product.getId()))
                .map(ProductInventory::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal stockOf(Material material) {

        MaterialInventory inventory = materialStock.get(material.getId());

        return inventory == null
                ? BigDecimal.ZERO
                : inventory.getQuantity();
    }

    private void increaseMaterialStock(
            Material material,
            BigDecimal quantity
    ) {

        MaterialInventory inventory = resolveMaterialInventory(material);

        inventory.setQuantity(inventory.getQuantity().add(quantity));

        materialInventoryRepository.save(inventory);

    }

    private void decreaseMaterialStock(
            Material material,
            BigDecimal quantity
    ) {

        MaterialInventory inventory = resolveMaterialInventory(material);

        inventory.setQuantity(inventory.getQuantity().subtract(quantity));

        materialInventoryRepository.save(inventory);

    }

    private MaterialInventory resolveMaterialInventory(Material material) {

        return materialStock.computeIfAbsent(material.getId(), key ->
                materialInventoryRepository
                        .findByWarehouseIdAndMaterialId(
                                materialWarehouse.getId(),
                                key
                        )
                        .orElseGet(() -> materialInventoryRepository.save(
                                factory.materialInventory(
                                        materialWarehouse,
                                        material
                                )
                        ))
        );
    }

    private void recordTransaction(
            Material material,
            InventoryTransactionType type,
            BigDecimal quantity,
            String referenceNo,
            User createdBy,
            LocalDateTime occurredAt
    ) {

        InventoryTransaction transaction = transactionRepository.save(
                factory.inventoryTransaction(
                        materialWarehouse,
                        material,
                        type,
                        quantity,
                        referenceNo,
                        createdBy
                )
        );

        transactionTimestamps
                .computeIfAbsent(occurredAt, key -> new ArrayList<>())
                .add(transaction.getId());

        touchedMaterials.add(material.getId());

    }

    private void applyTransactionTimestamps() {

        for (Map.Entry<LocalDateTime, List<Long>> entry
                : transactionTimestamps.entrySet()) {

            String ids = entry.getValue().stream()
                    .map(String::valueOf)
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");

            if (ids.isEmpty()) {
                continue;
            }

            entityManager.createNativeQuery(
                            "UPDATE inventory_transactions "
                                    + "SET created_at = :occurredAt "
                                    + "WHERE id IN (" + ids + ")"
                    )
                    .setParameter("occurredAt", entry.getKey())
                    .executeUpdate();

        }

    }

    private BigDecimal materialVariance(BigDecimal system) {

        if (random.nextInt(100) < 70) {
            return BigDecimal.ZERO;
        }

        int magnitude = 1 + random.nextInt(30);

        BigDecimal variance = random.nextBoolean()
                ? BigDecimal.valueOf(magnitude)
                : BigDecimal.valueOf(-magnitude);

        return system.add(variance).signum() < 0
                ? BigDecimal.ZERO
                : variance;
    }

    private BigDecimal productVariance(BigDecimal system) {

        if (system.signum() == 0) {
            return BigDecimal.ZERO;
        }

        int magnitude = 1 + random.nextInt(8);

        BigDecimal variance = random.nextInt(100) < 70
                ? BigDecimal.valueOf(-magnitude)
                : BigDecimal.valueOf(magnitude);

        return system.add(variance).signum() < 0
                ? BigDecimal.ZERO
                : variance;
    }

    private BigDecimal unitPriceOf(Material material) {

        BigDecimal base = materialPrices.get(material.getCode());

        BigDecimal factorValue =
                BigDecimal.valueOf(95 + random.nextInt(11))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return base.multiply(factorValue)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<Integer> pickProductLines(int count) {

        List<Integer> indexes = new ArrayList<>();

        for (int index = 0; index < products.size(); index++) {
            indexes.add(index);
        }

        Collections.shuffle(indexes, random);

        List<Integer> selected = new ArrayList<>(
                indexes.subList(0, Math.min(count, indexes.size()))
        );

        Collections.sort(selected);

        return selected;
    }

    private int[] split(int total, int[] weights) {

        int sum = 0;

        for (int weight : weights) {
            sum += weight;
        }

        int[] result = new int[weights.length];

        int assigned = 0;

        for (int index = 0; index < weights.length - 1; index++) {

            result[index] = Math.max(
                    10,
                    roundToTen(total * weights[index] / sum)
            );

            assigned += result[index];

        }

        result[weights.length - 1] = total - assigned;

        if (result[weights.length - 1] < 10) {

            result[0] += result[weights.length - 1] - 10;

            result[weights.length - 1] = 10;

        }

        return result;
    }

    private int roundToTen(int value) {

        return Math.max(10, (value / 10) * 10);
    }

    private LocalDate lastWorkingDay(LocalDate date) {

        return date.getDayOfWeek() == DayOfWeek.SUNDAY
                ? date.minusDays(1)
                : date;
    }

    private User nextOperator() {

        return operators.get(operatorCursor++ % operators.size());
    }

}
