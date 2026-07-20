package com.github.xnaut97.wms.service.stock;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.stocktaking.*;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.inventory.Inventory;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.entity.stock.Stocktaking;
import com.github.xnaut97.wms.entity.stock.StocktakingItem;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.inventory.InventoryRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingItemRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingRepository;
import com.github.xnaut97.wms.service.DocumentNumberService;
import com.github.xnaut97.wms.service.warehouse.RawMaterialService;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class StocktakingService {

    private final StocktakingRepository repository;

    private final WarehouseService warehouseService;

    private final UserService userService;

    private final DocumentNumberService documentNumberService;

    private final StocktakingItemRepository itemRepository;

    private final RawMaterialService materialService;

    private final InventoryRepository inventoryRepository;

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
    public StocktakingResponse getById(Long id) {

        return map(findById(id));

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

        stocktaking.setStatus(
                StocktakingStatus.DRAFT
        );

        stocktaking.setNote(
                request.getNote()
        );

        stocktaking.setCreatedBy(
                currentUser
        );

        repository.save(stocktaking);

        return map(stocktaking);

    }

    @Transactional
    public StocktakingItemResponse addItem(
            Long stocktakingId,
            AddStocktakingItemRequest request
    ) {

        if (itemRepository.existsByStocktakingIdAndMaterialId(
                stocktakingId,
                request.getMaterialId())) {

            throw new BusinessException(
                    "Nguyên liệu đã tồn tại trong phiếu kiểm kho này."
            );
        }

        Stocktaking stocktaking = findById(stocktakingId);

        if (stocktaking.getStatus() != StocktakingStatus.DRAFT) {
            throw new BusinessException(
                    "Không thể chỉnh sửa phiếu kiểm kho đã xác nhận."
            );
        }

        RawMaterial material =
                materialService.findMaterialById(
                        request.getMaterialId()
                );

        Inventory inventory = inventoryRepository
                        .findByWarehouseIdAndMaterialId(
                                stocktaking.getWarehouse().getId(),
                                material.getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Không tìm thấy tồn kho."
                                ));

        BigDecimal systemQuantity =
                inventory.getQuantity();

        BigDecimal physicalQuantity =
                request.getPhysicalQuantity();

        BigDecimal variance =
                physicalQuantity.subtract(systemQuantity);

        StocktakingItem item =
                new StocktakingItem();

        item.setStocktaking(stocktaking);

        item.setMaterial(material);

        item.setSystemQuantity(systemQuantity);

        item.setPhysicalQuantity(physicalQuantity);

        item.setVarianceQuantity(variance);

        itemRepository.save(item);

        return map(item);

    }

    @Transactional
    public StocktakingItemResponse updateItem(

            Long itemId,

            UpdateStocktakingItemRequest request

    ) {

        StocktakingItem item = itemRepository.findById(itemId)

                .orElseThrow(() ->

                        new BusinessException(
                                "Không tìm thấy dòng phiếu kiểm kho."
                        )

                );

        if (item.getStocktaking().getStatus() != StocktakingStatus.DRAFT) {

            throw new BusinessException(
                    "Không thể chỉnh sửa phiếu kiểm kho đã xác nhận."
            );

        }

        item.setPhysicalQuantity(
                request.getPhysicalQuantity()
        );

        item.setVarianceQuantity(

                request.getPhysicalQuantity()

                        .subtract(item.getSystemQuantity())

        );

        itemRepository.save(item);

        return map(item);

    }

    @Transactional
    public void confirm(Long stocktakingId) {

        Stocktaking stocktaking = findById(stocktakingId);

        if (stocktaking.getStatus() != StocktakingStatus.DRAFT) {
            throw new BusinessException(
                    "Phiếu kiểm kho đã được xác nhận."
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

            updateInventory(stocktaking, item);

            createInventoryTransaction(stocktaking, item);

        }

        stocktaking.setStatus(
                StocktakingStatus.CONFIRMED
        );

        repository.save(stocktaking);

    }

    private void updateInventory(

            Stocktaking stocktaking,

            StocktakingItem item

    ) {

        Inventory inventory = inventoryRepository

                .findByWarehouseIdAndMaterialId(

                        stocktaking.getWarehouse().getId(),

                        item.getMaterial().getId()

                )

                .orElseThrow(() ->

                        new BusinessException(
                                "Không tìm thấy tồn kho."
                        )

                );

        inventory.setQuantity(
                item.getPhysicalQuantity()
        );

        inventoryRepository.save(inventory);

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



    public Stocktaking findById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy phiếu kiểm kho."
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

        return StocktakingItemResponse.builder()

                .id(item.getId())

                .materialId(
                        item.getMaterial().getId()
                )

                .materialCode(
                        item.getMaterial().getCode()
                )

                .materialName(
                        item.getMaterial().getName()
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

                .warehouse(
                        stocktaking.getWarehouse().getName()
                )

                .stocktakingDate(
                        stocktaking.getStocktakingDate()
                )

                .status(
                        stocktaking.getStatus()
                )

                .note(
                        stocktaking.getNote()
                )

                .build();

    }

}