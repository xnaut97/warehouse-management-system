package com.github.xnaut97.wms.service.product;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.product.receipt.*;
import com.github.xnaut97.wms.entity.bom.BOM;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.entity.product.ProductReceipt;
import com.github.xnaut97.wms.entity.product.ProductReceiptItem;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.bom.BOMRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.product.ProductReceiptItemRepository;
import com.github.xnaut97.wms.repository.product.ProductReceiptRepository;
import com.github.xnaut97.wms.service.DocumentNumberService;
import com.github.xnaut97.wms.service.SupplierService;
import com.github.xnaut97.wms.service.user.UserService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReceiptService {

    private final ProductReceiptRepository repository;

    private final ProductReceiptItemRepository itemRepository;

    private final ProductInventoryRepository inventoryRepository;

    private final ProductService productService;

    private final BOMRepository bomRepository;

    private final SupplierService supplierService;

    private final WarehouseService warehouseService;

    private final UserService userService;

    private final DocumentNumberService generator;

    private static final int PRICE_SCALE = 2;

    public Page<ProductReceiptResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::map);
    }

    public ProductReceiptDetailResponse getDetail(Long id) {

        ProductReceipt receipt = findById(id);

        List<ProductReceiptItemResponse> items =
                itemRepository.findByReceiptId(id)
                        .stream()
                        .map(this::map)
                        .toList();

        return ProductReceiptDetailResponse.builder()
                .id(receipt.getId())
                .receiptNo(receipt.getReceiptNo())
                .supplier(
                        receipt.getSupplier() != null
                                ? receipt.getSupplier().getName()
                                : null
                )
                .warehouse(receipt.getWarehouse().getName())
                .receiptDate(receipt.getReceiptDate())
                .status(receipt.getStatus())
                .totalAmount(receipt.getTotalAmount())
                .createdBy(
                        receipt.getCreatedBy().getFullName() != null
                                ? receipt.getCreatedBy().getFullName()
                                : receipt.getCreatedBy().getUsername()
                )
                .items(items)
                .build();
    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "ProductReceipt"
    )
    @Transactional
    public ProductReceiptResponse create(
            ProductReceiptRequest request
    ) {

        Warehouse warehouse =
                warehouseService.findWarehouseByIdForGroup(
                        request.getWarehouseId(),
                        StockGroup.PRODUCT
                );

        Supplier supplier =
                request.getSupplierId() != null
                        ? supplierService.findSupplierById(
                        request.getSupplierId()
                )
                        : null;

        User currentUser = getCurrentUser();

        if (currentUser == null) {
            throw new BusinessException(
                    "Không tìm thấy người dùng"
            );
        }

        ProductReceipt receipt = new ProductReceipt();

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
            action = AuditAction.CREATE,
            entity = "ProductReceiptItem"
    )
    @Transactional
    public ProductReceiptItemResponse addItem(
            Long receiptId,
            AddProductReceiptItemRequest request
    ) {

        ProductReceipt receipt = findById(receiptId);

        validateDraft(receipt);

        if (itemRepository.existsByReceiptIdAndProductId(
                receiptId,
                request.getProductId()
        )) {
            throw new BusinessException(
                    "Sản phẩm đã tồn tại trong phiếu nhập."
            );
        }

        Product product =
                productService.findProductById(
                        request.getProductId()
                );

        BigDecimal unitPrice =
                calculateUnitPriceFromBOM(product);

        BigDecimal amount = calculateAmount(
                request.getQuantity(),
                unitPrice
        );

        ProductReceiptItem item =
                new ProductReceiptItem();

        item.setReceipt(receipt);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        item.setLotNumber(request.getLotNumber());
        item.setExpirationDate(request.getExpirationDate());
        item.setUnitPrice(unitPrice);
        item.setAmount(amount);

        itemRepository.save(item);

        updateTotal(receipt);

        return map(item);
    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "ProductReceiptItem"
    )
    @Transactional
    public ProductReceiptItemResponse updateItem(
            Long receiptId,
            Long itemId,
            UpdateProductReceiptItemRequest request
    ) {

        ProductReceipt receipt = findById(receiptId);

        validateDraft(receipt);

        ProductReceiptItem item =
                itemRepository.findByIdAndReceiptId(
                        itemId,
                        receiptId
                ).orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy dòng phiếu nhập."
                        )
                );

        BigDecimal unitPrice =
                calculateUnitPriceFromBOM(
                        item.getProduct()
                );

        item.setQuantity(request.getQuantity());
        item.setLotNumber(request.getLotNumber());
        item.setExpirationDate(request.getExpirationDate());
        item.setUnitPrice(unitPrice);

        item.setAmount(
                calculateAmount(
                        request.getQuantity(),
                        unitPrice
                )
        );

        itemRepository.save(item);

        updateTotal(receipt);

        return map(item);
    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "ProductReceiptItem"
    )
    @Transactional
    public void deleteItem(
            Long receiptId,
            Long itemId
    ) {

        ProductReceipt receipt = findById(receiptId);

        validateDraft(receipt);

        ProductReceiptItem item =
                itemRepository.findByIdAndReceiptId(
                        itemId,
                        receiptId
                ).orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy dòng phiếu nhập."
                        )
                );

        itemRepository.delete(item);

        updateTotal(receipt);
    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "ProductReceipt"
    )
    @Transactional
    public ProductReceiptResponse update(
            Long id,
            ProductReceiptRequest request
    ) {

        ProductReceipt receipt = findById(id);

        validateDraft(receipt);

        Warehouse warehouse =
                warehouseService.findWarehouseByIdForGroup(
                        request.getWarehouseId(),
                        StockGroup.PRODUCT
                );

        Supplier supplier =
                request.getSupplierId() != null
                        ? supplierService.findSupplierById(
                        request.getSupplierId()
                )
                        : null;

        receipt.setWarehouse(warehouse);
        receipt.setSupplier(supplier);
        receipt.setReceiptDate(request.getReceiptDate());

        repository.save(receipt);

        return map(receipt);
    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "ProductReceipt"
    )
    @Transactional
    public void delete(Long id) {

        ProductReceipt receipt = findById(id);

        validateDraft(receipt);

        repository.delete(receipt);
    }

    @Audit(
            action = AuditAction.CONFIRM,
            entity = "ProductReceipt"
    )
    @Transactional
    public void confirm(Long id) {

        ProductReceipt receipt = findById(id);

        validateDraft(receipt);

        List<ProductReceiptItem> items =
                itemRepository.findByReceiptId(id);

        if (items.isEmpty()) {
            throw new BusinessException(
                    "Phiếu nhập chưa có dòng hàng nào."
            );
        }

        for (ProductReceiptItem item : items) {
            updateInventory(receipt, item);
        }

        receipt.setStatus(ReceiptStatus.CONFIRMED);

        repository.save(receipt);

        // Giai đoạn 2: Tính lại Giá vốn trung bình (MAC) cho mỗi sản phẩm
        // Gom các dòng theo productId để tính tổng SL nhập & bình quân đơn giá
        Map<Long, List<ProductReceiptItem>> itemsByProduct =
                items.stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getProduct().getId()
                        ));

        for (Map.Entry<Long, List<ProductReceiptItem>> entry
                : itemsByProduct.entrySet()) {

            Long productId = entry.getKey();
            List<ProductReceiptItem> productItems = entry.getValue();

            BigDecimal totalNewQty = productItems.stream()
                    .map(ProductReceiptItem::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalNewValue = productItems.stream()
                    .map(item -> item.getQuantity()
                            .multiply(item.getUnitPrice() != null
                                    ? item.getUnitPrice()
                                    : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal weightedUnitPrice =
                    totalNewQty.compareTo(BigDecimal.ZERO) > 0
                            ? totalNewValue.divide(
                                    totalNewQty,
                                    PRICE_SCALE,
                                    RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

            productService.updateAverageCost(
                    productId,
                    totalNewQty,
                    weightedUnitPrice
            );
        }
    }

    public ProductReceipt findById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy phiếu nhập sản phẩm."
                        )
                );
    }

    private void updateInventory(
            ProductReceipt receipt,
            ProductReceiptItem item
    ) {

        String lotNumber = normalizeLot(item.getLotNumber());

        ProductInventory inventory =
                inventoryRepository
                        .findByWarehouseProductAndLot(
                                receipt.getWarehouse().getId(),
                                item.getProduct().getId(),
                                lotNumber
                        )
                        .orElseGet(() -> {

                            ProductInventory newInventory =
                                    new ProductInventory();

                            newInventory.setWarehouse(
                                    receipt.getWarehouse()
                            );

                            newInventory.setProduct(
                                    item.getProduct()
                            );

                            newInventory.setLotNumber(lotNumber);

                            newInventory.setQuantity(
                                    BigDecimal.ZERO
                            );

                            return newInventory;
                        });

        if (item.getExpirationDate() != null) {
            inventory.setExpirationDate(
                    item.getExpirationDate()
            );
        }

        inventory.setQuantity(
                inventory.getQuantity()
                        .add(item.getQuantity())
        );

        inventoryRepository.save(inventory);
    }

    private String normalizeLot(String lotNumber) {

        return lotNumber == null || lotNumber.isBlank()
                ? null
                : lotNumber.trim();
    }

    private void updateTotal(ProductReceipt receipt) {

        BigDecimal total =
                itemRepository.findByReceiptId(
                                receipt.getId()
                        )
                        .stream()
                        .map(ProductReceiptItem::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        receipt.setTotalAmount(total);

        repository.save(receipt);
    }

    private BigDecimal calculateAmount(
            BigDecimal quantity,
            BigDecimal unitPrice
    ) {

        if (unitPrice == null) {
            return null;
        }

        return quantity.multiply(unitPrice);
    }

    /**
     * Giai đoạn 1: Tính đơn giá nhập thành phẩm từ BOM × giá vốn NVL.
     *
     * Đơn giá nhập = ∑ (consumptionQuantity × giá vốn trung bình NVL)
     * (consumptionQuantity là lượng NVL cần cho 1 đơn vị SP)
     */
    private BigDecimal calculateUnitPriceFromBOM(Product product) {

        BOM bom = bomRepository
                .findFirstByProductIdAndEnabledTrue(
                        product.getId()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                String.format(
                                        "Không tìm thấy BOM cho sản phẩm %s. "
                                                + "Vui lòng tạo BOM trước khi nhập kho.",
                                        product.getName()
                                )
                        )
                );

        BigDecimal totalCost = bom.getItems()
                .stream()
                .map(bomItem ->
                        bomItem.getConsumptionQuantity()
                                .multiply(
                                        bomItem.getMaterial().getUnitPrice()
                                )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalCost.setScale(
                PRICE_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private void validateDraft(
            ProductReceipt receipt
    ) {

        if (receipt.getStatus() != ReceiptStatus.DRAFT) {
            throw new BusinessException(
                    "Chỉ có thể chỉnh sửa phiếu nhập ở trạng thái nháp."
            );
        }
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return userService.findByUsername("admin");
        }

        return userService.findByUsername(
                authentication.getName()
        );
    }

    private ProductReceiptResponse map(
            ProductReceipt receipt
    ) {

        return ProductReceiptResponse.builder()
                .id(receipt.getId())
                .receiptNo(receipt.getReceiptNo())
                .supplier(
                        receipt.getSupplier() != null
                                ? receipt.getSupplier().getName()
                                : null
                )
                .warehouse(receipt.getWarehouse().getName())
                .receiptDate(receipt.getReceiptDate())
                .status(receipt.getStatus())
                .totalAmount(receipt.getTotalAmount())
                .build();
    }

    private ProductReceiptItemResponse map(
            ProductReceiptItem item
    ) {

        return ProductReceiptItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productCode(item.getProduct().getCode())
                .productName(item.getProduct().getName())
                .unit(item.getProduct().getUnit())
                .quantity(item.getQuantity())
                .lotNumber(item.getLotNumber())
                .expirationDate(item.getExpirationDate())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .build();
    }
}