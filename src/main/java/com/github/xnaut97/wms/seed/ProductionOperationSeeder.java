package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.bom.BOMItemResponse;
import com.github.xnaut97.wms.dto.bom.BOMResponse;
import com.github.xnaut97.wms.dto.inventory.ProductStockResponse;
import com.github.xnaut97.wms.dto.issue.AddIssueItemRequest;
import com.github.xnaut97.wms.dto.issue.IssueRequest;
import com.github.xnaut97.wms.dto.product.issue.AddProductIssueItemRequest;
import com.github.xnaut97.wms.dto.product.issue.ProductIssueRequest;
import com.github.xnaut97.wms.dto.product.receipt.AddProductReceiptItemRequest;
import com.github.xnaut97.wms.dto.product.receipt.ProductReceiptRequest;
import com.github.xnaut97.wms.dto.receipt.AddReceiptItemRequest;
import com.github.xnaut97.wms.dto.receipt.ReceiptRequest;
import com.github.xnaut97.wms.dto.stocktaking.SaveStocktakingCountRequest;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingCountLineRequest;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingDetailResponse;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingItemBatchResponse;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingItemResponse;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingRequest;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.StocktakingType;
import com.github.xnaut97.wms.repository.CustomerRepository;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.SupplierRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import com.github.xnaut97.wms.service.DocumentNumberService;
import com.github.xnaut97.wms.service.IssueService;
import com.github.xnaut97.wms.service.ReceiptService;
import com.github.xnaut97.wms.service.bom.BOMService;
import com.github.xnaut97.wms.service.inventory.ProductStockService;
import com.github.xnaut97.wms.service.product.ProductIssueService;
import com.github.xnaut97.wms.service.product.ProductReceiptService;
import com.github.xnaut97.wms.service.stock.StocktakingService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProductionOperationSeeder {

    private record BomLine(
            Long materialId,
            String materialCode,
            String unit,
            BigDecimal consumptionQuantity,
            BigDecimal maxWasteRatio
    ) {
    }

    private record ProductLine(
            Long id,
            String code,
            ProductSeeder.Recipe recipe,
            boolean highGrade,
            List<BomLine> bom
    ) {
    }

    private record MaterialRef(
            Long id,
            String code,
            Long supplierId,
            MaterialSeeder.Recipe recipe
    ) {
    }

    private record ReceiptDraw(
            MaterialRef material,
            BigDecimal quantity
    ) {
    }

    private record LotTake(
            String lotNumber,
            BigDecimal quantity
    ) {
    }

    private record CountLine(
            StocktakingCountLineRequest request,
            BigDecimal systemQuantity,
            List<String> reasons
    ) {
    }

    private record StocktakePlan(
            boolean productWarehouse,
            boolean withVariance,
            boolean balanced
    ) {
    }

    private static final long RANDOM_SEED = 20260901L;

    private static final int BUILD_UP_DAYS = 42;

    private static final int MIN_DAILY_OUTPUT = 800;

    private static final int MAX_DAILY_OUTPUT = 900;

    private static final double SALES_RATIO = 0.95;

    private static final double[] SEASON_INDEX = {
            0.785, 0.724, 0.815, 0.845, 0.876, 0.906,
            0.967, 1.089, 1.180, 1.271, 1.332, 1.210
    };

    private static final DateTimeFormatter LOT_DATE =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final int RUN_DOWN_DAYS = 60;

    private static final int NOMINAL_MONTH_DAYS = 26;

    private static final double BUILD_UP_SALES_RATIO = 0.40;

    private static final Set<String> RUN_DOWN_MATERIALS = Set.of(
            MaterialSeeder.HPMC,
            MaterialSeeder.RDP,
            MaterialSeeder.packagingCode("TP-C1-20")
    );

    private static final int[] NEAR_EXPIRY_OFFSETS = {45, 70, 85};

    private static final int[] NEAR_EXPIRY_FLOORS = {45, 80, 120};

    private static final BigDecimal FINAL_BULK_SAND =
            BigDecimal.valueOf(76800);

    private static final List<String> MATERIAL_VARIANCE_REASONS = List.of(
            "Sai lệch khi cân lại thực tế",
            "Hao hụt trong quá trình bảo quản",
            "Ghi nhận thiếu phiếu xuất sản xuất",
            "Rách bao trong quá trình bốc dỡ"
    );

    private static final List<String> PRODUCT_VARIANCE_REASONS = List.of(
            "Đếm sai khi kiểm kê lần đầu",
            "Bao bì hư hỏng phải loại bỏ",
            "Thất thoát trong quá trình đảo kho",
            "Ghi nhận sai lô khi xuất hàng"
    );

    private final WarehouseRepository warehouseRepository;

    private final UserRepository userRepository;

    private final MaterialRepository materialRepository;

    private final ProductRepository productRepository;

    private final SupplierRepository supplierRepository;

    private final CustomerRepository customerRepository;

    private final MaterialInventoryRepository materialInventoryRepository;

    private final ProductInventoryRepository productInventoryRepository;

    private final ReceiptService receiptService;

    private final IssueService issueService;

    private final ProductReceiptService productReceiptService;

    private final ProductIssueService productIssueService;

    private final ProductStockService productStockService;

    private final StocktakingService stocktakingService;

    private final BOMService bomService;

    private final DocumentNumberService documentNumberService;

    private final JdbcTemplate jdbcTemplate;

    private Random random;

    private Warehouse materialWarehouse;

    private Warehouse productWarehouse;

    private List<User> operators;

    private List<Customer> customers;

    private List<ProductLine> productLines;

    private Map<Long, MaterialRef> materialsById;

    private Map<String, BigDecimal> lotFloors;

    private Map<LocalDate, Integer> salesPlan;

    private Map<LocalDate, List<StocktakePlan>> stocktakingPlan;

    private Set<LocalDate> highGradeDays;

    private Set<LocalDate> nearExpiryDays;

    private LocalDate buildUpStart;

    private LocalDate windowStart;

    private LocalDate salesStart;

    private LocalDate end;

    private LocalDate runDownFrom;

    private int operatorCursor;

    private int customerCursor;

    public void seed() {

        Product primary = productRepository
                .findByCode(ProductSeeder.PRIMARY_CODE)
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

            System.out.println("✓ Production operations already seeded");

            return;
        }

        if (!prepare()) {

            System.out.println(
                    "! Production operations skipped, no BOM configured"
            );

            return;
        }

        LocalDateTime runStart = LocalDateTime.now().minusMinutes(1);

        for (LocalDate date = buildUpStart;
             !date.isAfter(end);
             date = date.plusDays(1)) {

            if (isWorkingDay(date)) {

                Map<Long, Integer> plan = productionPlan(date);

                Map<Long, BigDecimal> requirement = materialRequirement(plan);

                replenishMaterials(date, requirement);

                seedProductionIssue(date, requirement);

                seedProductionReceipt(date, plan);

            }

            if (!date.isBefore(salesStart)) {
                seedSales(date);
            }

            for (StocktakePlan plan
                    : stocktakingPlan.getOrDefault(date, List.of())) {

                seedStocktaking(date, plan);

            }

        }

        seedFinalBulkDelivery();

        SecurityContextHolder.clearContext();

        applyBusinessTimestamps(runStart);

        System.out.println("✓ Production operations seeded");

    }

    private boolean prepare() {

        random = new Random(RANDOM_SEED);

        operators = List.of(
                userRepository.findByUsername("staff").orElseThrow(),
                userRepository.findByUsername("manager").orElseThrow(),
                userRepository.findByUsername("admin").orElseThrow()
        );

        customers = CustomerSeeder.CODES.stream()
                .map(code -> customerRepository.findByCode(code).orElseThrow())
                .toList();

        Map<Long, List<BomLine>> bomsByProduct = new LinkedHashMap<>();

        for (BOMResponse bom : bomService.getAll()) {

            if (!Boolean.TRUE.equals(bom.getEnabled())) {
                continue;
            }

            bomsByProduct.put(
                    bom.getProductId(),
                    bom.getItems().stream()
                            .map(this::bomLine)
                            .toList()
            );

        }

        materialsById = new LinkedHashMap<>();

        productLines = new ArrayList<>();

        for (ProductSeeder.Recipe recipe : ProductSeeder.RECIPES) {

            Product product = productRepository
                    .findByCode(recipe.code())
                    .orElse(null);

            if (product == null) {
                continue;
            }

            List<BomLine> bom = bomsByProduct.get(product.getId());

            if (bom == null || bom.isEmpty()) {
                continue;
            }

            bom.forEach(this::registerMaterial);

            productLines.add(
                    new ProductLine(
                            product.getId(),
                            product.getCode(),
                            recipe,
                            ProductSeeder.C2_CATEGORY.equals(recipe.category()),
                            bom
                    )
            );

        }

        if (productLines.stream().noneMatch(line -> !line.highGrade())) {
            return false;
        }

        end = LocalDate.now();

        windowStart = end.minusMonths(12).withDayOfMonth(1);

        buildUpStart = windowStart.minusDays(BUILD_UP_DAYS);

        salesStart = buildUpStart.plusDays(14);

        runDownFrom = end.minusDays(RUN_DOWN_DAYS);

        lotFloors = new LinkedHashMap<>();

        planHighGradeDays();

        planNearExpiryDays();

        planSales();

        planStocktaking();

        return true;

    }

    private BomLine bomLine(BOMItemResponse item) {

        return new BomLine(
                item.getMaterialId(),
                item.getMaterialCode(),
                item.getUnit(),
                item.getConsumptionQuantity(),
                item.getMaxWasteRatio()
        );

    }

    private void registerMaterial(BomLine line) {

        materialsById.computeIfAbsent(line.materialId(), id -> {

            Material material = materialRepository.findById(id).orElseThrow();

            MaterialSeeder.Recipe recipe =
                    MaterialSeeder.findRecipe(material.getCode())
                            .orElseGet(() -> fallbackRecipe(material));

            Long supplierId = supplierRepository
                    .findByCode(recipe.supplierCode())
                    .map(supplier -> supplier.getId())
                    .orElse(null);

            return new MaterialRef(
                    id,
                    material.getCode(),
                    supplierId,
                    recipe
            );

        });

    }

    private MaterialSeeder.Recipe fallbackRecipe(Material material) {

        BigDecimal minimum = material.getMinimumStock().max(BigDecimal.ONE);

        return new MaterialSeeder.Recipe(
                material.getCode(),
                material.getName(),
                material.getUnit(),
                material.getUnitPrice(),
                "",
                material.getMinimumStock(),
                material.getMaximumStock(),
                BigDecimal.ONE,
                minimum,
                material.getMaximumStock().max(minimum),
                10
        );

    }

    private void planHighGradeDays() {

        highGradeDays = new LinkedHashSet<>();

        if (productLines.stream().noneMatch(ProductLine::highGrade)) {
            return;
        }

        Map<YearMonth, List<LocalDate>> byMonth = workingDaysByMonth();

        for (List<LocalDate> days : byMonth.values()) {

            if (days.size() < 6) {
                continue;
            }

            int periods = 1 + random.nextInt(3);

            for (int period = 0; period < periods; period++) {

                int start = random.nextInt(days.size() - 2);

                int length = 1 + random.nextInt(2);

                for (int offset = 0;
                     offset < length && start + offset < days.size();
                     offset++) {

                    highGradeDays.add(days.get(start + offset));

                }

            }

        }

    }

    private void planNearExpiryDays() {

        nearExpiryDays = new LinkedHashSet<>();

        for (int offset : NEAR_EXPIRY_OFFSETS) {

            LocalDate target = end.plusDays(offset).minusYears(1);

            LocalDate candidate = target;

            while (!isWorkingDay(candidate)
                    || candidate.isBefore(windowStart)) {

                candidate = candidate.plusDays(1);

            }

            nearExpiryDays.add(candidate);

        }

    }

    private void planSales() {

        salesPlan = new LinkedHashMap<>();

        for (Map.Entry<YearMonth, List<LocalDate>> entry
                : workingDaysByMonth().entrySet()) {

            List<LocalDate> days = entry.getValue().stream()
                    .filter(date -> !date.isBefore(salesStart))
                    .toList();

            if (days.isEmpty()) {
                continue;
            }

            double index =
                    SEASON_INDEX[entry.getKey().getMonthValue() - 1];

            double average =
                    (MIN_DAILY_OUTPUT + MAX_DAILY_OUTPUT) / 2.0;

            double coverage =
                    (double) days.size() / workingDaysIn(entry.getKey());

            double factor = days.getFirst().isBefore(windowStart)
                    ? BUILD_UP_SALES_RATIO
                    : 1.0;

            int target = (int) Math.round(
                    NOMINAL_MONTH_DAYS * average * index
                            * SALES_RATIO * factor * coverage
            );

            double[] weights = new double[days.size()];

            double total = 0;

            for (int index2 = 0; index2 < days.size(); index2++) {

                weights[index2] = 0.6 + random.nextDouble() * 0.8;

                total += weights[index2];

            }

            int assigned = 0;

            for (int position = 0; position < days.size(); position++) {

                int quantity = position == days.size() - 1
                        ? target - assigned
                        : roundToTen(
                        (int) Math.round(target * weights[position] / total)
                );

                assigned += quantity;

                salesPlan.put(days.get(position), Math.max(quantity, 0));

            }

        }

    }

    private void planStocktaking() {

        stocktakingPlan = new LinkedHashMap<>();

        List<LocalDate> materialCounts = new ArrayList<>();

        List<LocalDate> productCounts = new ArrayList<>();

        for (List<LocalDate> days : workingDaysByMonth().values()) {

            if (days.size() < 8) {
                continue;
            }

            int total = 4 + random.nextInt(3);

            List<Integer> slots = new ArrayList<>();

            for (int position = 0; position < total; position++) {

                int slot = (days.size() * (position + 1)) / (total + 1);

                if (!slots.contains(slot)) {
                    slots.add(slot);
                }

            }

            for (int position = 0; position < slots.size(); position++) {

                LocalDate date = days.get(slots.get(position));

                if (position == slots.size() - 1) {
                    productCounts.add(date);
                } else {
                    materialCounts.add(date);
                }

            }

        }

        register(materialCounts, false);

        register(productCounts, true);

    }

    private void register(
            List<LocalDate> dates,
            boolean productWarehouseCount
    ) {

        for (int position = 0; position < dates.size(); position++) {

            boolean last = position == dates.size() - 1;

            boolean variance = last || random.nextInt(100) < 25;

            stocktakingPlan
                    .computeIfAbsent(
                            dates.get(position),
                            date -> new ArrayList<>()
                    )
                    .add(
                            new StocktakePlan(
                                    productWarehouseCount,
                                    variance,
                                    !last
                            )
                    );

        }

    }

    private int workingDaysIn(YearMonth month) {

        int days = 0;

        for (int day = 1; day <= month.lengthOfMonth(); day++) {

            if (isWorkingDay(month.atDay(day))) {
                days++;
            }

        }

        return days;

    }

    private Map<YearMonth, List<LocalDate>> workingDaysByMonth() {

        Map<YearMonth, List<LocalDate>> byMonth = new LinkedHashMap<>();

        for (LocalDate date = buildUpStart;
             !date.isAfter(end);
             date = date.plusDays(1)) {

            if (!isWorkingDay(date)) {
                continue;
            }

            byMonth
                    .computeIfAbsent(
                            YearMonth.from(date),
                            month -> new ArrayList<>()
                    )
                    .add(date);

        }

        return byMonth;

    }

    private Map<Long, Integer> productionPlan(LocalDate date) {

        int total = MIN_DAILY_OUTPUT
                + random.nextInt(MAX_DAILY_OUTPUT - MIN_DAILY_OUTPUT + 1);

        Map<Long, Integer> plan = new LinkedHashMap<>();

        int remaining = total;

        if (highGradeDays.contains(date)) {

            ProductLine highGrade = productLines.stream()
                    .filter(ProductLine::highGrade)
                    .findFirst()
                    .orElseThrow();

            int share = roundToTen(
                    total * (50 + random.nextInt(31)) / 100
            );

            plan.put(highGrade.id(), share);

            remaining -= share;

        }

        List<ProductLine> standard = productLines.stream()
                .filter(line -> !line.highGrade())
                .toList();

        List<ProductLine> selected = standard.size() > 1
                && random.nextInt(100) < 70
                ? standard
                : List.of(weightedPick(standard));

        int[] weights = selected.stream()
                .mapToInt(line -> line.recipe().outputWeight())
                .toArray();

        int[] quantities = split(remaining, weights);

        for (int position = 0; position < selected.size(); position++) {

            if (quantities[position] <= 0) {
                continue;
            }

            plan.merge(
                    selected.get(position).id(),
                    quantities[position],
                    Integer::sum
            );

        }

        return plan;

    }

    private Map<Long, BigDecimal> materialRequirement(Map<Long, Integer> plan) {

        Map<Long, BigDecimal> requirement = new LinkedHashMap<>();

        for (Map.Entry<Long, Integer> entry : plan.entrySet()) {

            ProductLine line = lineOf(entry.getKey());

            BigDecimal produced = BigDecimal.valueOf(entry.getValue());

            for (BomLine bom : line.bom()) {

                BigDecimal base =
                        bom.consumptionQuantity().multiply(produced);

                BigDecimal ceiling = base.add(
                        base.multiply(bom.maxWasteRatio())
                                .divide(
                                        BigDecimal.valueOf(100),
                                        6,
                                        RoundingMode.HALF_UP
                                )
                );

                BigDecimal used = base.add(
                        ceiling.subtract(base)
                                .multiply(
                                        BigDecimal.valueOf(random.nextDouble())
                                )
                );

                requirement.merge(
                        bom.materialId(),
                        quantize(used.min(ceiling), base, bom.unit()),
                        BigDecimal::add
                );

            }

        }

        return requirement;

    }

    private BigDecimal quantize(
            BigDecimal quantity,
            BigDecimal base,
            String unit
    ) {

        if (!MaterialSeeder.PIECE_UNIT.equals(unit)) {
            return quantity.setScale(2, RoundingMode.DOWN);
        }

        BigDecimal pieces = quantity.setScale(0, RoundingMode.FLOOR);

        return pieces.compareTo(base) < 0
                ? base.setScale(0, RoundingMode.CEILING)
                : pieces;

    }

    private void replenishMaterials(
            LocalDate date,
            Map<Long, BigDecimal> requirement
    ) {

        List<List<ReceiptDraw>> rounds = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : requirement.entrySet()) {

            MaterialRef material = materialsById.get(entry.getKey());

            BigDecimal need = entry.getValue();

            boolean runDown =
                    RUN_DOWN_MATERIALS.contains(material.code())
                            && !date.isBefore(runDownFrom);

            BigDecimal target = runDown
                    ? need
                    : need.multiply(
                    BigDecimal.valueOf(material.recipe().coverDays())
            );

            BigDecimal stock = stockOf(material);

            BigDecimal threshold = need.add(target);

            int round = 0;

            while (stock.compareTo(threshold) < 0 && round < 8) {

                BigDecimal quantity = drawQuantity(
                        material,
                        threshold.subtract(stock),
                        need,
                        runDown
                );

                while (rounds.size() <= round) {
                    rounds.add(new ArrayList<>());
                }

                rounds.get(round).add(new ReceiptDraw(material, quantity));

                stock = stock.add(quantity);

                round++;

            }

        }

        LocalDate receiptDate = date.isAfter(buildUpStart)
                ? date.minusDays(1)
                : date;

        for (List<ReceiptDraw> draws : rounds) {

            Map<Long, List<ReceiptDraw>> bySupplier = new LinkedHashMap<>();

            for (ReceiptDraw draw : draws) {

                bySupplier
                        .computeIfAbsent(
                                draw.material().supplierId(),
                                supplier -> new ArrayList<>()
                        )
                        .add(draw);

            }

            bySupplier.forEach((supplierId, lines) ->
                    seedMaterialReceipt(receiptDate, supplierId, lines));

        }

    }

    private BigDecimal drawQuantity(
            MaterialRef material,
            BigDecimal deficit,
            BigDecimal need,
            boolean runDown
    ) {

        MaterialSeeder.Recipe recipe = material.recipe();

        BigDecimal lot = recipe.receiptLotSize();

        long units = ceilUnits(deficit, lot);

        if (runDown) {

            return lot.multiply(
                    BigDecimal.valueOf(
                            Math.max(units, ceilUnits(need, lot))
                    )
            );

        }

        long minimum = ceilUnits(recipe.receiptMinimum(), lot);

        long maximum = Math.max(
                recipe.receiptMaximum()
                        .divideToIntegralValue(lot)
                        .longValue(),
                minimum
        );

        units = Math.min(Math.max(units, minimum), maximum);

        if (units < maximum) {

            units += random.nextInt((int) (maximum - units + 1));

        }

        return lot.multiply(BigDecimal.valueOf(units));

    }

    private long ceilUnits(
            BigDecimal quantity,
            BigDecimal lot
    ) {

        return Math.max(
                quantity.divide(lot, 0, RoundingMode.CEILING).longValue(),
                1
        );

    }

    private void seedMaterialReceipt(
            LocalDate date,
            Long supplierId,
            List<ReceiptDraw> lines
    ) {

        authenticate(nextOperator());

        ReceiptRequest request = new ReceiptRequest();

        request.setSupplierId(supplierId);

        request.setWarehouseId(materialWarehouse.getId());

        request.setReceiptDate(date);

        Long receiptId = receiptService.create(request).getId();

        renumber(
                "goods_receipts",
                "receipt_no",
                receiptId,
                DocumentType.GOODS_RECEIPT,
                date
        );

        for (ReceiptDraw draw : lines) {

            AddReceiptItemRequest item = new AddReceiptItemRequest();

            item.setMaterialId(draw.material().id());

            item.setQuantity(draw.quantity());

            item.setUnitPrice(purchasePrice(draw.material()));

            receiptService.addItem(receiptId, item);

        }

        receiptService.confirm(receiptId);

    }

    private BigDecimal purchasePrice(MaterialRef material) {

        double drift = 0.96 + random.nextDouble() * 0.08;

        return material.recipe().unitPrice()
                .multiply(BigDecimal.valueOf(drift))
                .setScale(2, RoundingMode.HALF_UP);

    }

    private void seedProductionIssue(
            LocalDate date,
            Map<Long, BigDecimal> requirement
    ) {

        authenticate(nextOperator());

        IssueRequest request = new IssueRequest();

        request.setWarehouseId(materialWarehouse.getId());

        request.setIssueDate(date);

        Long issueId = issueService.create(request).getId();

        renumber(
                "goods_issues",
                "issue_no",
                issueId,
                DocumentType.GOODS_ISSUE,
                date
        );

        for (Map.Entry<Long, BigDecimal> entry : requirement.entrySet()) {

            AddIssueItemRequest item = new AddIssueItemRequest();

            item.setMaterialId(entry.getKey());

            item.setQuantity(entry.getValue());

            item.setUnitPrice(currentPrice(entry.getKey()));

            issueService.addItem(issueId, item);

        }

        issueService.confirm(issueId);

    }

    private BigDecimal currentPrice(Long materialId) {

        return materialRepository.findById(materialId)
                .orElseThrow()
                .getUnitPrice();

    }

    private void seedProductionReceipt(
            LocalDate date,
            Map<Long, Integer> plan
    ) {

        authenticate(nextOperator());

        ProductReceiptRequest request = new ProductReceiptRequest();

        request.setWarehouseId(productWarehouse.getId());

        request.setReceiptDate(date);

        Long receiptId = productReceiptService.create(request).getId();

        renumber(
                "product_receipts",
                "receipt_no",
                receiptId,
                DocumentType.GOODS_RECEIPT,
                date
        );

        for (Map.Entry<Long, Integer> entry : plan.entrySet()) {

            ProductLine line = lineOf(entry.getKey());

            String lotNumber = lotNumber(line, date);

            AddProductReceiptItemRequest item =
                    new AddProductReceiptItemRequest();

            item.setProductId(line.id());

            item.setQuantity(BigDecimal.valueOf(entry.getValue()));

            item.setLotNumber(lotNumber);

            item.setExpirationDate(date.plusYears(1));

            item.setUnitPrice(line.recipe().costPrice());

            productReceiptService.addItem(receiptId, item);

            reserveNearExpiry(date, lotNumber);

        }

        productReceiptService.confirm(receiptId);

    }

    private void reserveNearExpiry(
            LocalDate date,
            String lotNumber
    ) {

        if (!nearExpiryDays.contains(date) || lotFloors.containsKey(lotNumber)) {
            return;
        }

        int position = Math.min(lotFloors.size(), NEAR_EXPIRY_FLOORS.length - 1);

        lotFloors.put(
                lotNumber,
                BigDecimal.valueOf(NEAR_EXPIRY_FLOORS[position])
        );

    }

    private String lotNumber(
            ProductLine line,
            LocalDate date
    ) {

        return "LOT-%s-%s".formatted(
                line.code(),
                date.format(LOT_DATE)
        );

    }

    private void seedSales(LocalDate date) {

        int planned = salesPlan.getOrDefault(date, 0);

        if (planned <= 0) {
            return;
        }

        Map<Long, BigDecimal> available = availableByProduct();

        BigDecimal total = available.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.signum() <= 0) {
            return;
        }

        for (Map.Entry<Long, BigDecimal> entry : available.entrySet()) {

            BigDecimal share = BigDecimal.valueOf(planned)
                    .multiply(entry.getValue())
                    .divide(total, 0, RoundingMode.DOWN);

            int quantity = roundToTen(
                    share.min(entry.getValue()).intValue()
            );

            if (quantity < 20) {
                continue;
            }

            int orders = random.nextInt(100) < 30 ? 2 : 1;

            int assigned = 0;

            for (int order = 0; order < orders; order++) {

                int part = order == orders - 1
                        ? quantity - assigned
                        : roundToTen(quantity / orders);

                assigned += part;

                if (part >= 20) {
                    seedSalesIssue(date, entry.getKey(), part);
                }

            }

        }

    }

    private Map<Long, BigDecimal> availableByProduct() {

        Map<Long, BigDecimal> available = new LinkedHashMap<>();

        for (ProductStockResponse lot
                : productStockService.getAvailableLots(
                productWarehouse.getId(),
                null
        )) {

            BigDecimal free = freeQuantity(lot);

            if (free.signum() <= 0) {
                continue;
            }

            available.merge(lot.getProductId(), free, BigDecimal::add);

        }

        return available;

    }

    private BigDecimal freeQuantity(ProductStockResponse lot) {

        BigDecimal floor = lotFloors.getOrDefault(
                lot.getLotNumber(),
                BigDecimal.ZERO
        );

        return lot.getQuantity().subtract(floor).max(BigDecimal.ZERO);

    }

    private void seedSalesIssue(
            LocalDate date,
            Long productId,
            int quantity
    ) {

        List<LotTake> takes = allocate(productId, quantity);

        if (takes.isEmpty()) {
            return;
        }

        Customer customer = nextCustomer();

        authenticate(nextOperator());

        ProductIssueRequest request = new ProductIssueRequest();

        request.setWarehouseId(productWarehouse.getId());

        request.setCustomerId(customer.getId());

        request.setIssueDate(date);

        Long issueId = productIssueService.create(request).getId();

        renumber(
                "product_issues",
                "issue_no",
                issueId,
                DocumentType.GOODS_ISSUE,
                date
        );

        BigDecimal price = salePrice(productId, customer);

        for (LotTake take : takes) {

            AddProductIssueItemRequest item =
                    new AddProductIssueItemRequest();

            item.setProductId(productId);

            item.setQuantity(take.quantity());

            item.setLotNumber(take.lotNumber());

            item.setUnitPrice(price);

            productIssueService.addItem(issueId, item);

        }

        productIssueService.confirm(issueId);

    }

    private List<LotTake> allocate(
            Long productId,
            int quantity
    ) {

        List<LotTake> takes = new ArrayList<>();

        BigDecimal remaining = BigDecimal.valueOf(quantity);

        for (ProductStockResponse lot
                : productStockService.getAvailableLots(
                productWarehouse.getId(),
                productId
        )) {

            if (remaining.signum() <= 0) {
                break;
            }

            BigDecimal take = freeQuantity(lot).min(remaining);

            if (take.signum() <= 0) {
                continue;
            }

            takes.add(new LotTake(lot.getLotNumber(), take));

            remaining = remaining.subtract(take);

        }

        return takes;

    }

    private BigDecimal salePrice(
            Long productId,
            Customer customer
    ) {

        double factor = switch (customer.getCustomerGroup()) {
            case PROJECT -> 0.96;
            case AGENT -> 0.99;
            case RETAIL -> 1.04;
        };

        return lineOf(productId).recipe().salePrice()
                .multiply(BigDecimal.valueOf(factor))
                .setScale(2, RoundingMode.HALF_UP);

    }

    private void seedStocktaking(
            LocalDate date,
            StocktakePlan plan
    ) {

        Warehouse warehouse = plan.productWarehouse()
                ? productWarehouse
                : materialWarehouse;

        authenticate(nextOperator());

        StocktakingRequest request = new StocktakingRequest();

        request.setWarehouseId(warehouse.getId());

        request.setStocktakingDate(date);

        request.setType(
                random.nextInt(100) < 80
                        ? StocktakingType.PERIODIC
                        : StocktakingType.AD_HOC
        );

        request.setNote(
                plan.productWarehouse()
                        ? "Kiểm kê kho thành phẩm"
                        : "Kiểm kê kho nguyên vật liệu"
        );

        Long stocktakingId = stocktakingService.create(request).getId();

        renumber(
                "stocktaking",
                "stocktaking_no",
                stocktakingId,
                DocumentType.INVENTORY_CHECK,
                date
        );

        StocktakingDetailResponse detail =
                stocktakingService.getById(stocktakingId);

        SaveStocktakingCountRequest count =
                countRequest(detail, plan.withVariance());

        if (count.getItems().isEmpty() && count.getBatches().isEmpty()) {
            return;
        }

        stocktakingService.confirm(stocktakingId, count);

        if (plan.balanced()) {
            stocktakingService.balance(stocktakingId);
        }

    }

    private SaveStocktakingCountRequest countRequest(
            StocktakingDetailResponse detail,
            boolean withVariance
    ) {

        SaveStocktakingCountRequest request =
                new SaveStocktakingCountRequest();

        List<StocktakingCountLineRequest> items = new ArrayList<>();

        List<StocktakingCountLineRequest> batches = new ArrayList<>();

        List<CountLine> countable = new ArrayList<>();

        for (StocktakingItemResponse item : detail.getItems()) {

            if (item.isBatchManaged()) {

                for (StocktakingItemBatchResponse batch : item.getBatches()) {

                    batches.add(
                            countLine(
                                    batch.getId(),
                                    batch.getSystemQuantity(),
                                    countable,
                                    PRODUCT_VARIANCE_REASONS
                            )
                    );

                }

                continue;

            }

            items.add(
                    countLine(
                            item.getId(),
                            item.getSystemQuantity(),
                            countable,
                            MATERIAL_VARIANCE_REASONS
                    )
            );

        }

        if (withVariance && !countable.isEmpty()) {

            Collections.shuffle(countable, random);

            int lines = Math.min(
                    1 + random.nextInt(3),
                    countable.size()
            );

            for (int position = 0; position < lines; position++) {
                applyVariance(countable.get(position));
            }

        }

        request.setItems(items);

        request.setBatches(batches);

        return request;

    }

    private StocktakingCountLineRequest countLine(
            Long id,
            BigDecimal systemQuantity,
            List<CountLine> countable,
            List<String> reasons
    ) {

        StocktakingCountLineRequest line =
                new StocktakingCountLineRequest();

        line.setId(id);

        line.setPhysicalQuantity(systemQuantity);

        if (systemQuantity.signum() > 0) {

            countable.add(new CountLine(line, systemQuantity, reasons));

        }

        return line;

    }

    private void applyVariance(CountLine countLine) {

        BigDecimal systemQuantity = countLine.systemQuantity();

        double ratio = (0.5 + random.nextDouble() * 2.5) / 100;

        BigDecimal delta = systemQuantity
                .multiply(BigDecimal.valueOf(ratio))
                .setScale(2, RoundingMode.HALF_UP)
                .max(BigDecimal.valueOf(0.01));

        BigDecimal physical = random.nextBoolean()
                ? systemQuantity.subtract(delta)
                : systemQuantity.add(delta);

        countLine.request().setPhysicalQuantity(physical.max(BigDecimal.ZERO));

        countLine.request().setReason(
                countLine.reasons().get(
                        random.nextInt(countLine.reasons().size())
                )
        );

    }

    private void seedFinalBulkDelivery() {

        MaterialRef sand = materialsById.values().stream()
                .filter(material -> MaterialSeeder.SAND.equals(material.code()))
                .findFirst()
                .orElse(null);

        if (sand == null) {
            return;
        }

        seedMaterialReceipt(
                end,
                sand.supplierId(),
                List.of(new ReceiptDraw(sand, FINAL_BULK_SAND))
        );

    }

    private void applyBusinessTimestamps(LocalDateTime runStart) {

        backdate(
                "goods_receipts",
                "receipt_date",
                450,
                runStart
        );

        backdate(
                "goods_issues",
                "issue_date",
                555,
                runStart
        );

        backdate(
                "product_receipts",
                "receipt_date",
                980,
                runStart
        );

        backdate(
                "product_issues",
                "issue_date",
                820,
                runStart
        );

        backdate(
                "stocktaking",
                "stocktaking_date",
                600,
                runStart
        );

        backdateChild("goods_receipt_items", "goods_receipts", "receipt_id", runStart);

        backdateChild("goods_issue_items", "goods_issues", "issue_id", runStart);

        backdateChild("product_receipt_items", "product_receipts", "receipt_id", runStart);

        backdateChild("product_issue_items", "product_issues", "issue_id", runStart);

        backdateChild("stocktaking_item", "stocktaking", "stocktaking_id", runStart);

        jdbcTemplate.update("""
                UPDATE stocktaking_item_batches b
                JOIN stocktaking_item i ON i.id = b.item_id
                SET b.created_at = i.created_at,
                    b.updated_at = i.created_at
                WHERE b.created_at >= ?
                """, runStart);

        backdateTransactions("goods_receipts", "receipt_no", runStart);

        backdateTransactions("goods_issues", "issue_no", runStart);

        backdateTransactions("stocktaking", "stocktaking_no", runStart);

    }

    private void renumber(
            String table,
            String column,
            Long id,
            DocumentType type,
            LocalDate date
    ) {

        jdbcTemplate.update(
                "UPDATE %s SET %s = ? WHERE id = ?".formatted(table, column),
                documentNumberService.next(type, date),
                id
        );

    }

    private void backdate(
            String table,
            String dateColumn,
            int minutes,
            LocalDateTime runStart
    ) {

        jdbcTemplate.update(
                """
                        UPDATE %s
                        SET created_at = TIMESTAMP(%s)
                                + INTERVAL %d MINUTE
                                + INTERVAL (id %% 35) MINUTE,
                            updated_at = TIMESTAMP(%s)
                                + INTERVAL %d MINUTE
                                + INTERVAL (id %% 35) MINUTE
                        WHERE created_at >= ?
                        """.formatted(
                        table,
                        dateColumn,
                        minutes,
                        dateColumn,
                        minutes
                ),
                runStart
        );

    }

    private void backdateChild(
            String table,
            String parentTable,
            String foreignKey,
            LocalDateTime runStart
    ) {

        jdbcTemplate.update(
                """
                        UPDATE %s c
                        JOIN %s p ON p.id = c.%s
                        SET c.created_at = p.created_at,
                            c.updated_at = p.created_at
                        WHERE c.created_at >= ?
                        """.formatted(table, parentTable, foreignKey),
                runStart
        );

    }

    private void backdateTransactions(
            String table,
            String documentColumn,
            LocalDateTime runStart
    ) {

        jdbcTemplate.update(
                """
                        UPDATE inventory_transactions t
                        JOIN %s d ON d.%s = t.reference_no
                        SET t.created_at = d.created_at + INTERVAL 5 MINUTE,
                            t.updated_at = d.created_at + INTERVAL 5 MINUTE
                        WHERE t.created_at >= ?
                        """.formatted(table, documentColumn),
                runStart
        );

    }

    private BigDecimal stockOf(MaterialRef material) {

        return materialInventoryRepository
                .findByWarehouseIdAndMaterialId(
                        materialWarehouse.getId(),
                        material.id()
                )
                .map(inventory -> inventory.getQuantity())
                .orElse(BigDecimal.ZERO);

    }

    private ProductLine lineOf(Long productId) {

        return productLines.stream()
                .filter(line -> line.id().equals(productId))
                .findFirst()
                .orElseThrow();

    }

    private ProductLine weightedPick(List<ProductLine> lines) {

        int total = lines.stream()
                .mapToInt(line -> line.recipe().outputWeight())
                .sum();

        int pick = random.nextInt(total);

        for (ProductLine line : lines) {

            pick -= line.recipe().outputWeight();

            if (pick < 0) {
                return line;
            }

        }

        return lines.getLast();

    }

    private int[] split(
            int total,
            int[] weights
    ) {

        int[] result = new int[weights.length];

        int sum = 0;

        for (int weight : weights) {
            sum += weight;
        }

        int assigned = 0;

        for (int position = 0; position < weights.length; position++) {

            if (position == weights.length - 1) {

                result[position] = total - assigned;

                break;

            }

            result[position] = roundToTen(total * weights[position] / sum);

            assigned += result[position];

        }

        return result;

    }

    private int roundToTen(int value) {

        return Math.max(value / 10 * 10, 0);

    }

    private boolean isWorkingDay(LocalDate date) {

        return date.getDayOfWeek() != DayOfWeek.SUNDAY;

    }

    private User nextOperator() {

        return operators.get(operatorCursor++ % operators.size());

    }

    private Customer nextCustomer() {

        return customers.get(customerCursor++ % customers.size());

    }

    private void authenticate(User user) {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        List.of()
                )
        );

    }

}
