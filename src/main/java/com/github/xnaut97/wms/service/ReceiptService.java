package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.receipt.*;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.inventory.Inventory;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.entity.goods.GoodsReceipt;
import com.github.xnaut97.wms.entity.goods.GoodsReceiptItem;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import com.github.xnaut97.wms.service.user.UserService;
import com.github.xnaut97.wms.service.warehouse.RawMaterialService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final GoodsReceiptRepository repository;

    private final SupplierService supplierService;

    private final WarehouseService warehouseService;

    private final UserService userService;

    private final DocumentNumberService generator;

    private final GoodsReceiptItemRepository itemRepository;

    private final RawMaterialService materialService;

    private final InventoryRepository inventoryRepository;

    private final InventoryTransactionRepository transactionRepository;

    public Page<ReceiptResponse> getAll(Pageable pageable) {

        return repository.findAll(pageable)
                .map(this::map);

    }

    public ReceiptDetailResponse getDetail(Long id) {

        GoodsReceipt receipt = findReceiptById(id);

        List<ReceiptItemResponse> items =
                itemRepository.findByReceiptId(id)
                        .stream()
                        .map(this::map)
                        .toList();

        return ReceiptDetailResponse.builder()
                .id(receipt.getId())
                .receiptNo(receipt.getReceiptNo())
                .supplier(receipt.getSupplier().getName())
                .warehouse(receipt.getWarehouse().getName())
                .receiptDate(receipt.getReceiptDate())
                .status(receipt.getStatus())
                .totalAmount(receipt.getTotalAmount())
                .items(items)
                .build();

    }

    @Audit(
            action = AuditAction.CONFIRM,
            entity = "GoodsReceipt"
    )
    @Transactional
    public void confirm(Long receiptId) {

        GoodsReceipt receipt = findReceiptById(receiptId);

        validateDraft(receipt);

        List<GoodsReceiptItem> items =
                itemRepository.findByReceiptId(receiptId);

        if (items.isEmpty()) {
            throw new BusinessException(
                    "Phiếu nhập chưa có dòng hàng nào."
            );
        }

        for (GoodsReceiptItem item : items) {

            updateInventory(receipt, item);

            createInventoryTransaction(receipt, item);

        }

        receipt.setStatus(ReceiptStatus.CONFIRMED);

        repository.save(receipt);

    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "GoodsReceiptItem"
    )
    @Transactional
    public ReceiptItemResponse addItem(
            Long receiptId,
            AddReceiptItemRequest request
    ) {
        if (itemRepository.existsByReceiptIdAndMaterialId(
                receiptId,
                request.getMaterialId())) {

            throw new BusinessException(
                    "Nguyên liệu này đã tồn tại trong phiếu nhập."
            );
        }

        GoodsReceipt receipt = findReceiptById(receiptId);

        if (receipt.getStatus() != ReceiptStatus.DRAFT) {
            throw new BusinessException(
                    "Không thể chỉnh sửa phiếu nhập đã xác nhận."
            );
        }


        RawMaterial material =
                materialService.findMaterialById(request.getMaterialId());

        GoodsReceiptItem item = new GoodsReceiptItem();

        item.setReceipt(receipt);
        item.setMaterial(material);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());

        BigDecimal amount =
                request.getQuantity()
                        .multiply(request.getUnitPrice());

        item.setAmount(amount);

        itemRepository.save(item);

        updateReceiptTotal(receipt);

        return map(item);

    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "GoodsReceipt"
    )
    @Transactional
    public ReceiptResponse create(ReceiptRequest request) {

        Supplier supplier =
                supplierService.findSupplierById(request.getSupplierId());

        Warehouse warehouse =
                warehouseService.findWarehouseById(request.getWarehouseId());

        User currentUser = getCurrentUser();
        if(currentUser == null)
            throw new BusinessException("Không tìm thấy người dùng");

        GoodsReceipt receipt = new GoodsReceipt();

        receipt.setReceiptNo(
                generator.next(DocumentType.GOODS_RECEIPT)
        );
        receipt.setSupplier(supplier);
        receipt.setWarehouse(warehouse);
        receipt.setReceiptDate(request.getReceiptDate());
        receipt.setStatus(ReceiptStatus.DRAFT);
        receipt.setCreatedBy(currentUser);
        receipt.setTotalAmount(BigDecimal.ZERO);

        repository.save(receipt);

        return map(receipt);
    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "GoodsReceiptItem"
    )
    @Transactional
    public ReceiptItemResponse updateItem(
            Long receiptId,
            Long itemId,
            UpdateReceiptItemRequest request
    ) {

        GoodsReceipt receipt =
                findReceiptById(receiptId);

        validateDraft(receipt);

        GoodsReceiptItem item =
                findReceiptItem(receiptId, itemId);

        item.setQuantity(request.getQuantity());

        item.setUnitPrice(request.getUnitPrice());

        item.setAmount(

                request.getQuantity()

                        .multiply(request.getUnitPrice())

        );

        itemRepository.save(item);

        updateReceiptTotal(receipt);

        return map(item);

    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "GoodsReceiptItem"
    )
    @Transactional
    public void deleteItem(
            Long receiptId,
            Long itemId
    ){

        GoodsReceipt receipt =
                findReceiptById(receiptId);

        validateDraft(receipt);

        GoodsReceiptItem item =
                findReceiptItem(receiptId,itemId);

        itemRepository.delete(item);

        updateReceiptTotal(receipt);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "GoodsReceipt"
    )
    @Transactional
    public ReceiptResponse update(
            Long id,
            UpdateReceiptRequest request
    ){

        GoodsReceipt receipt =
                findReceiptById(id);

        validateDraft(receipt);

        Supplier supplier =
                supplierService.findSupplierById(
                        request.getSupplierId()
                );

        Warehouse warehouse =
                warehouseService.findWarehouseById(
                        request.getWarehouseId()
                );

        receipt.setSupplier(supplier);

        receipt.setWarehouse(warehouse);

        receipt.setReceiptDate(request.getReceiptDate());

        repository.save(receipt);

        return map(receipt);

    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "GoodsReceipt"
    )
    @Transactional
    public void delete(
            Long id
    ){

        GoodsReceipt receipt =
                findReceiptById(id);

        validateDraft(receipt);

        itemRepository.deleteAll(
                itemRepository.findByReceiptId(id)
        );

        repository.delete(receipt);

    }

    public GoodsReceipt findReceiptById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("Không tìm thấy phiếu nhập"));

    }

    private void updateReceiptTotal(GoodsReceipt receipt) {

        BigDecimal total = itemRepository
                .findByReceiptId(receipt.getId())
                .stream()
                .map(GoodsReceiptItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        receipt.setTotalAmount(total);

        repository.save(receipt);

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

    private void updateInventory(
            GoodsReceipt receipt,
            GoodsReceiptItem item
    ) {

        Inventory inventory =
                inventoryRepository
                        .findByWarehouseIdAndMaterialId(

                                receipt.getWarehouse().getId(),

                                item.getMaterial().getId()

                        )
                        .orElseGet(() -> {

                            Inventory inv = new Inventory();

                            inv.setWarehouse(receipt.getWarehouse());

                            inv.setMaterial(item.getMaterial());

                            inv.setQuantity(BigDecimal.ZERO);

                            return inv;

                        });

        inventory.setQuantity(

                inventory.getQuantity()

                        .add(item.getQuantity())

        );

        inventoryRepository.save(inventory);

    }

    private void createInventoryTransaction(
            GoodsReceipt receipt,
            GoodsReceiptItem item
    ) {

        InventoryTransaction transaction =
                new InventoryTransaction();

        transaction.setWarehouse(
                receipt.getWarehouse()
        );

        transaction.setMaterial(
                item.getMaterial()
        );

        transaction.setQuantity(
                item.getQuantity()
        );

        transaction.setReferenceNo(
                receipt.getReceiptNo()
        );

        transaction.setType(
                InventoryTransactionType.IN
        );

        transaction.setCreatedBy(
                receipt.getCreatedBy()
        );

        transactionRepository.save(transaction);

    }

    private GoodsReceiptItem findReceiptItem(
            Long receiptId,
            Long itemId
    ) {

        return itemRepository
                .findByIdAndReceiptId(itemId, receiptId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy dòng phiếu nhập"
                        ));

    }

    private void validateDraft(GoodsReceipt receipt) {

        if (receipt.getStatus() != ReceiptStatus.DRAFT) {
            throw new BusinessException(
                    "Chỉ có thể chỉnh sửa phiếu nhập ở trạng thái nháp."
            );
        }

    }

    private ReceiptItemResponse map(
            GoodsReceiptItem item
    ) {

        return ReceiptItemResponse.builder()
                .id(item.getId())
                .materialId(item.getMaterial().getId())
                .materialCode(item.getMaterial().getCode())
                .materialName(item.getMaterial().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .build();

    }

    private ReceiptResponse map(GoodsReceipt receipt) {

        return ReceiptResponse.builder()
                .id(receipt.getId())
                .receiptNo(receipt.getReceiptNo())
                .supplier(receipt.getSupplier().getName())
                .warehouse(receipt.getWarehouse().getName())
                .receiptDate(receipt.getReceiptDate())
                .status(receipt.getStatus())
                .totalAmount(receipt.getTotalAmount())
                .build();

    }
}