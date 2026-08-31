package com.github.xnaut97.wms.service.stock;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.stocktaking.*;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.entity.stock.Stocktaking;
import com.github.xnaut97.wms.entity.stock.StocktakingItem;
import com.github.xnaut97.wms.entity.stock.StocktakingItemBatch;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StocktakingItemStatus;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import com.github.xnaut97.wms.enums.StocktakingType;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingItemBatchRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingItemRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingRepository;
import com.github.xnaut97.wms.service.DocumentNumberService;
import com.github.xnaut97.wms.service.product.ProductService;
import com.github.xnaut97.wms.service.warehouse.MaterialService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import com.github.xnaut97.wms.service.user.UserService;
import com.github.xnaut97.wms.specification.StocktakingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StocktakingService {

    private final StocktakingRepository repository;

    private final WarehouseService warehouseService;

    private final UserService userService;

    private final DocumentNumberService documentNumberService;

    private final StocktakingItemRepository itemRepository;

    private final StocktakingItemBatchRepository batchRepository;

    private final MaterialService materialService;

    private final ProductService productService;

    private final MaterialInventoryRepository materialInventoryRepository;

    private final ProductInventoryRepository productInventoryRepository;

    private final InventoryTransactionRepository transactionRepository;

    @Transactional
    public Page<StocktakingResponse> getAll(

            String keyword,

            Long warehouseId,

            StocktakingStatus status,

            LocalDate fromDate,

            LocalDate toDate,

            Pageable pageable

    ) {

        return repository.findAll(

                        StocktakingSpecification.search(

                                keyword,

                                warehouseId,

                                status,

                                fromDate,

                                toDate

                        ),

                        pageable

                )

                .map(this::map);

    }

    @Transactional
    public StocktakingDetailResponse getById(Long id) {

        Stocktaking stocktaking = findById(id);

        List<StocktakingItemResponse> items =
                itemRepository.findByStocktakingId(id)
                        .stream()
                        .map(this::map)
                        .toList();

        return StocktakingDetailResponse.builder()

                .id(stocktaking.getId())

                .stocktakingNo(
                        stocktaking.getStocktakingNo()
                )

                .warehouseId(
                        stocktaking.getWarehouse().getId()
                )

                .warehouse(
                        stocktaking.getWarehouse().getName()
                )

                .warehouseGroup(
                        resolveGroup(stocktaking.getWarehouse())
                )

                .stocktakingDate(
                        stocktaking.getStocktakingDate()
                )

                .type(
                        stocktaking.getType()
                )

                .status(
                        stocktaking.getStatus()
                )

                .stocktaker(
                        resolveStocktakerName(stocktaking)
                )

                .note(
                        stocktaking.getNote()
                )

                .items(items)

                .build();

    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "Stocktaking"
    )
    @Transactional
    public StocktakingResponse create(
            StocktakingRequest request
    ) {

        Warehouse warehouse =
                warehouseService.findWarehouseById(
                        request.getWarehouseId()
                );

        User currentUser = getCurrentUser();

        User stocktaker =
                request.getStocktakerId() != null
                        ? userService.findUserById(
                        request.getStocktakerId()
                )
                        : currentUser;

        Stocktaking stocktaking = new Stocktaking();

        stocktaking.setStocktakingNo(
                documentNumberService.next(
                        DocumentType.INVENTORY_CHECK
                )
        );

        stocktaking.setWarehouse(
                warehouse
        );

        stocktaking.setStocktakingDate(
                request.getStocktakingDate()
        );

        stocktaking.setType(
                request.getType() != null
                        ? request.getType()
                        : StocktakingType.PERIODIC
        );

        stocktaking.setStatus(
                StocktakingStatus.IN_PROGRESS
        );

        stocktaking.setNote(
                request.getNote()
        );

        stocktaking.setStocktaker(
                stocktaker
        );

        stocktaking.setCreatedBy(
                currentUser
        );

        repository.save(stocktaking);

        populateItems(stocktaking);

        return map(stocktaking);

    }

    private void populateItems(Stocktaking stocktaking) {

        if (resolveGroup(stocktaking.getWarehouse()) == StockGroup.PRODUCT) {

            populateProductItems(stocktaking);

            return;

        }

        populateMaterialItems(stocktaking);

    }

    private void populateMaterialItems(Stocktaking stocktaking) {

        List<MaterialInventory> inventories = materialInventoryRepository
                .findAllByWarehouseIdOrderByMaterialCodeAsc(
                        stocktaking.getWarehouse().getId()
                );

        for (MaterialInventory inventory : inventories) {

            StocktakingItem item = new StocktakingItem();

            item.setStocktaking(stocktaking);

            item.setMaterial(
                    inventory.getMaterial()
            );

            item.setItemGroup(StockGroup.MATERIAL);

            item.setSystemQuantity(
                    inventory.getQuantity()
            );

            applyPhysicalQuantity(item, null);

            itemRepository.save(item);

        }

    }

    private void populateProductItems(Stocktaking stocktaking) {

        List<ProductInventory> inventories = productInventoryRepository
                .findAllByWarehouseIdOrderByProductCodeAscLotNumberAsc(
                        stocktaking.getWarehouse().getId()
                );

        Map<Long, StocktakingItem> itemsByProduct = new LinkedHashMap<>();

        for (ProductInventory inventory : inventories) {

            Product product = inventory.getProduct();

            StocktakingItem item = itemsByProduct.get(product.getId());

            if (item == null) {

                item = new StocktakingItem();

                item.setStocktaking(stocktaking);

                item.setProduct(product);

                item.setItemGroup(StockGroup.PRODUCT);

                item.setSystemQuantity(BigDecimal.ZERO);

                applyPhysicalQuantity(item, null);

                itemsByProduct.put(product.getId(), item);

            }

            item.setSystemQuantity(
                    item.getSystemQuantity().add(
                            inventory.getQuantity()
                    )
            );

            itemRepository.save(item);

            createBatch(item, inventory);

        }

    }

    private void createBatch(
            StocktakingItem item,
            ProductInventory inventory
    ) {

        StocktakingItemBatch batch = new StocktakingItemBatch();

        batch.setItem(item);

        batch.setProductInventory(inventory);

        batch.setLotNumber(
                inventory.getLotNumber()
        );

        batch.setExpirationDate(
                inventory.getExpirationDate()
        );

        batch.setSystemQuantity(
                inventory.getQuantity()
        );

        batch.setPhysicalQuantity(null);

        batch.setVarianceQuantity(BigDecimal.ZERO);

        batchRepository.save(batch);

    }

    @Transactional
    public StocktakingItemResponse addItem(
            Long stocktakingId,
            AddStocktakingItemRequest request
    ) {

        Stocktaking stocktaking = findById(stocktakingId);

        validateInProgress(stocktaking);

        StockGroup group = resolveGroup(stocktaking.getWarehouse());

        if (group == StockGroup.MATERIAL) {

            return map(
                    addMaterialItem(stocktaking, request)
            );

        }

        return map(
                addProductItem(stocktaking, request)
        );

    }

    private StocktakingItem addMaterialItem(
            Stocktaking stocktaking,
            AddStocktakingItemRequest request
    ) {

        if (request.getMaterialId() == null) {
            throw new BusinessException(
                    "Vui lòng chọn nguyên vật liệu."
            );
        }

        if (itemRepository.existsByStocktakingIdAndMaterialId(
                stocktaking.getId(),
                request.getMaterialId())) {

            throw new BusinessException(
                    "Nguyên liệu đã tồn tại trong phiếu kiểm kho này."
            );
        }

        Material material =
                materialService.findMaterialById(
                        request.getMaterialId()
                );

        MaterialInventory materialInventory = materialInventoryRepository
                .findByWarehouseIdAndMaterialId(
                        stocktaking.getWarehouse().getId(),
                        material.getId()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy tồn kho."
                        ));

        StocktakingItem item = new StocktakingItem();

        item.setStocktaking(stocktaking);

        item.setMaterial(material);

        item.setItemGroup(StockGroup.MATERIAL);

        item.setSystemQuantity(
                materialInventory.getQuantity()
        );

        item.setReason(
                request.getReason()
        );

        applyPhysicalQuantity(
                item,
                request.getPhysicalQuantity()
        );

        return itemRepository.save(item);

    }

    private StocktakingItem addProductItem(
            Stocktaking stocktaking,
            AddStocktakingItemRequest request
    ) {

        if (request.getProductId() == null) {
            throw new BusinessException(
                    "Vui lòng chọn thành phẩm."
            );
        }

        if (itemRepository.existsByStocktakingIdAndProductId(
                stocktaking.getId(),
                request.getProductId())) {

            throw new BusinessException(
                    "Thành phẩm đã tồn tại trong phiếu kiểm kho này."
            );
        }

        Product product =
                productService.findProductById(
                        request.getProductId()
                );

        List<ProductInventory> inventories =
                productInventoryRepository
                        .findAllByWarehouseIdAndProductId(
                                stocktaking.getWarehouse().getId(),
                                product.getId()
                        );

        if (inventories.isEmpty()) {
            throw new BusinessException(
                    "Không tìm thấy tồn kho."
            );
        }

        BigDecimal systemQuantity = inventories.stream()
                .map(ProductInventory::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StocktakingItem item = new StocktakingItem();

        item.setStocktaking(stocktaking);

        item.setProduct(product);

        item.setItemGroup(StockGroup.PRODUCT);

        item.setSystemQuantity(systemQuantity);

        item.setReason(
                request.getReason()
        );

        applyPhysicalQuantity(item, null);

        itemRepository.save(item);

        for (ProductInventory inventory : inventories) {

            createBatch(item, inventory);

        }

        return item;

    }

    @Transactional
    public StocktakingItemResponse updateItem(

            Long itemId,

            UpdateStocktakingItemRequest request

    ) {

        StocktakingItem item = findItemById(itemId);

        validateInProgress(item.getStocktaking());

        if (isBatchManaged(item)) {

            throw new BusinessException(
                    "Số lượng thực tế của thành phẩm phải được nhập theo từng lô."
            );

        }

        applyPhysicalQuantity(
                item,
                request.getPhysicalQuantity()
        );

        item.setReason(
                request.getReason()
        );

        itemRepository.save(item);

        return map(item);

    }

    @Transactional
    public StocktakingItemResponse updateBatch(

            Long batchId,

            UpdateStocktakingItemBatchRequest request

    ) {

        StocktakingItemBatch batch = batchRepository.findById(batchId)

                .orElseThrow(() ->

                        new BusinessException(
                                "Không tìm thấy lô hàng của phiếu kiểm kho."
                        )

                );

        StocktakingItem item = batch.getItem();

        validateInProgress(item.getStocktaking());

        batch.setPhysicalQuantity(
                request.getPhysicalQuantity()
        );

        batch.setVarianceQuantity(
                request.getPhysicalQuantity()
                        .subtract(batch.getSystemQuantity())
        );

        batch.setReason(
                request.getReason()
        );

        batchRepository.save(batch);

        rollUpBatches(item);

        return map(item);

    }

    private void rollUpBatches(StocktakingItem item) {

        List<StocktakingItemBatch> batches =
                batchRepository.findByItemId(item.getId());

        boolean incomplete = batches.isEmpty()
                || batches.stream()
                .anyMatch(batch -> batch.getPhysicalQuantity() == null);

        applyPhysicalQuantity(
                item,
                incomplete
                        ? null
                        : batches.stream()
                        .map(StocktakingItemBatch::getPhysicalQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        itemRepository.save(item);

    }

    @Audit(
            action = AuditAction.CONFIRM,
            entity = "Stocktaking"
    )
    @Transactional
    public void confirm(
            Long stocktakingId,
            SaveStocktakingCountRequest request
    ) {

        Stocktaking stocktaking = findById(stocktakingId);

        if (stocktaking.getStatus() == StocktakingStatus.STOCK_BALANCED) {
            throw new BusinessException(
                    "Phiếu kiểm kho đã được cân bằng tồn kho."
            );
        }

        if (stocktaking.getStatus() != StocktakingStatus.IN_PROGRESS) {
            throw new BusinessException(
                    "Phiếu kiểm kho đã được chốt số lượng thực tế."
            );
        }

        List<StocktakingItem> items =
                itemRepository.findByStocktakingId(stocktakingId);

        if (items.isEmpty()) {
            throw new BusinessException(
                    "Phiếu kiểm kho chưa có dòng hàng nào."
            );
        }

        if (request != null) {

            saveCount(stocktakingId, items, request);

            items = itemRepository.findByStocktakingId(stocktakingId);

        }

        for (StocktakingItem item : items) {

            if (isBatchManaged(item)) {

                List<StocktakingItemBatch> batches =
                        batchRepository.findByItemId(item.getId());

                boolean incomplete = batches.isEmpty()
                        || batches.stream()
                        .anyMatch(batch -> batch.getPhysicalQuantity() == null);

                if (incomplete) {
                    throw new BusinessException(
                            "Vui lòng nhập số lượng thực tế cho tất cả các lô."
                    );
                }

                continue;

            }

            if (item.getPhysicalQuantity() == null) {
                throw new BusinessException(
                        "Vui lòng nhập số lượng thực tế cho tất cả các dòng hàng."
                );
            }

        }

        stocktaking.setStatus(
                StocktakingStatus.COUNT_CONFIRMED
        );

        repository.save(stocktaking);

    }

    private void saveCount(

            Long stocktakingId,

            List<StocktakingItem> items,

            SaveStocktakingCountRequest request

    ) {

        Map<Long, StocktakingItem> itemsById = items.stream()
                .collect(Collectors.toMap(
                        StocktakingItem::getId,
                        item -> item
                ));

        Set<Long> touchedItemIds = new LinkedHashSet<>();

        if (request.getBatches() != null) {

            Map<Long, StocktakingItemBatch> batchesById =
                    batchRepository.findByItemStocktakingId(stocktakingId)
                            .stream()
                            .collect(Collectors.toMap(
                                    StocktakingItemBatch::getId,
                                    batch -> batch
                            ));

            for (StocktakingCountLineRequest line : request.getBatches()) {

                StocktakingItemBatch batch = batchesById.get(line.getId());

                if (batch == null) {
                    throw new BusinessException(
                            "Không tìm thấy lô hàng của phiếu kiểm kho."
                    );
                }

                batch.setPhysicalQuantity(
                        line.getPhysicalQuantity()
                );

                batch.setVarianceQuantity(
                        line.getPhysicalQuantity() == null
                                ? BigDecimal.ZERO
                                : line.getPhysicalQuantity()
                                .subtract(batch.getSystemQuantity())
                );

                batch.setReason(
                        line.getReason()
                );

                batchRepository.save(batch);

                touchedItemIds.add(
                        batch.getItem().getId()
                );

            }

        }

        if (request.getItems() != null) {

            for (StocktakingCountLineRequest line : request.getItems()) {

                StocktakingItem item = itemsById.get(line.getId());

                if (item == null) {
                    throw new BusinessException(
                            "Không tìm thấy dòng phiếu kiểm kho."
                    );
                }

                item.setReason(
                        line.getReason()
                );

                if (isBatchManaged(item)) {

                    itemRepository.save(item);

                    continue;

                }

                applyPhysicalQuantity(
                        item,
                        line.getPhysicalQuantity()
                );

                itemRepository.save(item);

            }

        }

        for (Long itemId : touchedItemIds) {

            rollUpBatches(itemsById.get(itemId));

        }

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Stocktaking"
    )
    @Transactional
    public void balance(Long stocktakingId) {

        Stocktaking stocktaking = findById(stocktakingId);

        if (stocktaking.getStatus() == StocktakingStatus.IN_PROGRESS) {
            throw new BusinessException(
                    "Phiếu kiểm kho chưa được chốt số lượng thực tế."
            );
        }

        if (stocktaking.getStatus() == StocktakingStatus.STOCK_BALANCED) {
            throw new BusinessException(
                    "Phiếu kiểm kho đã được cân bằng tồn kho."
            );
        }

        List<StocktakingItem> items =
                itemRepository.findByStocktakingId(stocktakingId);

        if (items.isEmpty()) {
            throw new BusinessException(
                    "Phiếu kiểm kho chưa có dòng hàng nào."
            );
        }

        for (StocktakingItem item : items) {

            if (isBatchManaged(item)) {

                balanceProductItem(item);

                continue;

            }

            balanceMaterialItem(stocktaking, item);

        }

        stocktaking.setStatus(
                StocktakingStatus.STOCK_BALANCED
        );

        repository.save(stocktaking);

    }

    private void balanceMaterialItem(

            Stocktaking stocktaking,

            StocktakingItem item

    ) {

        if (item.getPhysicalQuantity() == null) {

            throw new BusinessException(
                    "Vui lòng nhập số lượng thực tế cho tất cả các dòng hàng."
            );

        }

        MaterialInventory materialInventory = materialInventoryRepository

                .findByWarehouseIdAndMaterialId(

                        stocktaking.getWarehouse().getId(),

                        item.getMaterial().getId()

                )

                .orElseThrow(() ->

                        new BusinessException(
                                "Không tìm thấy tồn kho."
                        )

                );

        item.setSystemQuantity(
                materialInventory.getQuantity()
        );

        applyPhysicalQuantity(
                item,
                item.getPhysicalQuantity()
        );

        itemRepository.save(item);

        materialInventory.setQuantity(
                item.getPhysicalQuantity()
        );

        materialInventoryRepository.save(materialInventory);

        createInventoryTransaction(stocktaking, item);

    }

    private void balanceProductItem(StocktakingItem item) {

        List<StocktakingItemBatch> batches =
                batchRepository.findByItemId(item.getId());

        if (batches.isEmpty()) {

            throw new BusinessException(
                    "Vui lòng nhập số lượng thực tế cho tất cả các lô."
            );

        }

        BigDecimal systemQuantity = BigDecimal.ZERO;

        for (StocktakingItemBatch batch : batches) {

            if (batch.getPhysicalQuantity() == null) {

                throw new BusinessException(
                        "Vui lòng nhập số lượng thực tế cho tất cả các lô."
                );

            }

            ProductInventory inventory = batch.getProductInventory();

            batch.setSystemQuantity(
                    inventory.getQuantity()
            );

            batch.setVarianceQuantity(
                    batch.getPhysicalQuantity()
                            .subtract(inventory.getQuantity())
            );

            batchRepository.save(batch);

            systemQuantity = systemQuantity.add(
                    inventory.getQuantity()
            );

            inventory.setQuantity(
                    batch.getPhysicalQuantity()
            );

            productInventoryRepository.save(inventory);

        }

        item.setSystemQuantity(systemQuantity);

        applyPhysicalQuantity(
                item,
                batches.stream()
                        .map(StocktakingItemBatch::getPhysicalQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        itemRepository.save(item);

    }

    private void createInventoryTransaction(

            Stocktaking stocktaking,

            StocktakingItem item

    ) {

        if (item.getVarianceQuantity().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        InventoryTransaction transaction =
                new InventoryTransaction();

        transaction.setWarehouse(
                stocktaking.getWarehouse()
        );

        transaction.setMaterial(
                item.getMaterial()
        );

        transaction.setType(
                InventoryTransactionType.ADJUSTMENT
        );

        transaction.setReferenceNo(
                stocktaking.getStocktakingNo()
        );

        transaction.setCreatedBy(
                stocktaking.getCreatedBy()
        );

        transaction.setQuantity(
                item.getVarianceQuantity().abs()
        );

        transactionRepository.save(transaction);

    }

    private void applyPhysicalQuantity(

            StocktakingItem item,

            BigDecimal physicalQuantity

    ) {

        item.setPhysicalQuantity(physicalQuantity);

        if (physicalQuantity == null) {

            item.setVarianceQuantity(BigDecimal.ZERO);

            item.setItemStatus(null);

            return;

        }

        BigDecimal variance =
                physicalQuantity.subtract(item.getSystemQuantity());

        item.setVarianceQuantity(variance);

        item.setItemStatus(
                variance.compareTo(BigDecimal.ZERO) == 0
                        ? StocktakingItemStatus.MATCHED
                        : StocktakingItemStatus.DISCREPANCY
        );

    }

    private void validateInProgress(Stocktaking stocktaking) {

        if (stocktaking.getStatus() != StocktakingStatus.IN_PROGRESS) {

            throw new BusinessException(
                    "Không thể chỉnh sửa phiếu kiểm kho đã chốt số lượng thực tế."
            );

        }

    }

    private boolean isBatchManaged(StocktakingItem item) {

        return item.getProduct() != null;

    }

    private StockGroup resolveGroup(Warehouse warehouse) {

        return WarehouseService.resolveGroup(warehouse);

    }

    private String resolveStocktakerName(Stocktaking stocktaking) {

        User stocktaker =
                stocktaking.getStocktaker() != null
                        ? stocktaking.getStocktaker()
                        : stocktaking.getCreatedBy();

        if (stocktaker == null) {
            return null;
        }

        return stocktaker.getFullName() != null
                ? stocktaker.getFullName()
                : stocktaker.getUsername();

    }

    public Stocktaking findById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy phiếu kiểm kho."
                        )
                );

    }

    private StocktakingItem findItemById(Long id) {

        return itemRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy dòng phiếu kiểm kho."
                        )
                );

    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return userService.findByUsername("admin");
        }

        return userService.findByUsername(authentication.getName());

    }

    private StocktakingItemResponse map(
            StocktakingItem item
    ) {

        boolean batchManaged = isBatchManaged(item);

        List<StocktakingItemBatchResponse> batches =
                batchManaged
                        ? batchRepository.findByItemId(item.getId())
                        .stream()
                        .map(this::map)
                        .toList()
                        : List.of();

        return StocktakingItemResponse.builder()

                .id(item.getId())

                .itemGroup(
                        batchManaged
                                ? StockGroup.PRODUCT
                                : StockGroup.MATERIAL
                )

                .materialId(
                        item.getMaterial() != null
                                ? item.getMaterial().getId()
                                : null
                )

                .productId(
                        item.getProduct() != null
                                ? item.getProduct().getId()
                                : null
                )

                .code(
                        batchManaged
                                ? item.getProduct().getCode()
                                : item.getMaterial().getCode()
                )

                .name(
                        batchManaged
                                ? item.getProduct().getName()
                                : item.getMaterial().getName()
                )

                .unit(
                        batchManaged
                                ? item.getProduct().getUnit()
                                : item.getMaterial().getUnit()
                )

                .systemQuantity(
                        item.getSystemQuantity()
                )

                .physicalQuantity(
                        item.getPhysicalQuantity()
                )

                .varianceQuantity(
                        item.getVarianceQuantity()
                )

                .itemStatus(
                        item.getItemStatus()
                )

                .reason(
                        item.getReason()
                )

                .batchManaged(batchManaged)

                .batches(batches)

                .build();

    }

    private StocktakingItemBatchResponse map(
            StocktakingItemBatch batch
    ) {

        return StocktakingItemBatchResponse.builder()

                .id(batch.getId())

                .lotNumber(
                        batch.getLotNumber()
                )

                .expirationDate(
                        batch.getExpirationDate()
                )

                .systemQuantity(
                        batch.getSystemQuantity()
                )

                .physicalQuantity(
                        batch.getPhysicalQuantity()
                )

                .varianceQuantity(
                        batch.getVarianceQuantity()
                )

                .reason(
                        batch.getReason()
                )

                .build();

    }

    private StocktakingResponse map(
            Stocktaking stocktaking
    ) {

        return StocktakingResponse.builder()

                .id(stocktaking.getId())

                .stocktakingNo(
                        stocktaking.getStocktakingNo()
                )

                .warehouseId(
                        stocktaking.getWarehouse().getId()
                )

                .warehouse(
                        stocktaking.getWarehouse().getName()
                )

                .warehouseGroup(
                        resolveGroup(stocktaking.getWarehouse())
                )

                .stocktakingDate(
                        stocktaking.getStocktakingDate()
                )

                .type(
                        stocktaking.getType()
                )

                .status(
                        stocktaking.getStatus()
                )

                .stocktaker(
                        resolveStocktakerName(stocktaking)
                )

                .note(
                        stocktaking.getNote()
                )

                .build();

    }

}
