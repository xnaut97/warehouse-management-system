package com.github.xnaut97.wms.service.product;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.product.issue.*;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.entity.product.ProductIssue;
import com.github.xnaut97.wms.entity.product.ProductIssueItem;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.product.ProductIssueItemRepository;
import com.github.xnaut97.wms.repository.product.ProductIssueRepository;
import com.github.xnaut97.wms.service.CustomerService;
import com.github.xnaut97.wms.service.DocumentNumberService;
import com.github.xnaut97.wms.service.user.UserService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductIssueService {

    private final ProductIssueRepository repository;

    private final ProductIssueItemRepository itemRepository;

    private final ProductInventoryRepository inventoryRepository;

    private final ProductService productService;

    private final WarehouseService warehouseService;

    private final CustomerService customerService;

    private final UserService userService;

    private final DocumentNumberService generator;

    public Page<ProductIssueResponse> getAll(
            Pageable pageable
    ) {

        return repository.findAll(pageable)
                .map(this::map);
    }

    public ProductIssueDetailResponse getDetail(
            Long id
    ) {

        ProductIssue issue = findById(id);

        List<ProductIssueItemResponse> items =
                itemRepository.findByIssueId(id)
                        .stream()
                        .map(this::map)
                        .toList();

        return ProductIssueDetailResponse.builder()
                .id(issue.getId())
                .issueNo(issue.getIssueNo())
                .warehouse(issue.getWarehouse().getName())
                .customer(
                        issue.getCustomer() != null
                                ? issue.getCustomer().getName()
                                : null
                )
                .issueDate(issue.getIssueDate())
                .status(issue.getStatus())
                .totalAmount(issue.getTotalAmount())
                .items(items)
                .build();
    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "ProductIssue"
    )
    @Transactional
    public ProductIssueResponse create(
            ProductIssueRequest request
    ) {

        Warehouse warehouse =
                warehouseService.findWarehouseById(
                        request.getWarehouseId()
                );

        Customer customer =
                request.getCustomerId() != null
                        ? customerService.findCustomerById(
                                request.getCustomerId()
                        )
                        : null;

        User currentUser =
                userService.findByUsername(
                        org.springframework.security.core.context.SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName()
                );

        ProductIssue issue = new ProductIssue();

        issue.setIssueNo(
                generator.next(DocumentType.GOODS_ISSUE)
        );

        issue.setWarehouse(warehouse);
        issue.setCustomer(customer);
        issue.setIssueDate(request.getIssueDate());
        issue.setStatus(IssueStatus.DRAFT);
        issue.setCreatedBy(currentUser);
        issue.setTotalAmount(BigDecimal.ZERO);

        repository.save(issue);

        return map(issue);
    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "ProductIssueItem"
    )
    @Transactional
    public ProductIssueItemResponse addItem(
            Long issueId,
            AddProductIssueItemRequest request
    ) {

        ProductIssue issue = findById(issueId);

        validateDraft(issue);

        if (itemRepository.existsByIssueIdAndProductId(
                issueId,
                request.getProductId()
        )) {
            throw new BusinessException(
                    "Sản phẩm đã tồn tại trong phiếu xuất."
            );
        }

        Product product =
                productService.findProductById(
                        request.getProductId()
                );

        ProductIssueItem item =
                new ProductIssueItem();

        item.setIssue(issue);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        item.setLotNumber(request.getLotNumber());
        item.setExpirationDate(request.getExpirationDate());
        item.setUnitPrice(request.getUnitPrice());
        item.setAmount(
                calculateAmount(
                        request.getQuantity(),
                        request.getUnitPrice()
                )
        );

        itemRepository.save(item);

        updateTotal(issue);

        return map(item);
    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "ProductIssueItem"
    )
    @Transactional
    public ProductIssueItemResponse updateItem(
            Long issueId,
            Long itemId,
            UpdateProductIssueItemRequest request
    ) {

        ProductIssue issue = findById(issueId);

        validateDraft(issue);

        ProductIssueItem item =
                itemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Không tìm thấy dòng phiếu xuất."
                                )
                        );

        if (!item.getIssue().getId().equals(issueId)) {
            throw new BusinessException(
                    "Dòng phiếu xuất không thuộc phiếu này."
            );
        }

        item.setQuantity(request.getQuantity());
        item.setLotNumber(request.getLotNumber());
        item.setExpirationDate(request.getExpirationDate());
        item.setUnitPrice(request.getUnitPrice());

        item.setAmount(
                calculateAmount(
                        request.getQuantity(),
                        request.getUnitPrice()
                )
        );

        itemRepository.save(item);

        updateTotal(issue);

        return map(item);
    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "ProductIssueItem"
    )
    @Transactional
    public void deleteItem(
            Long issueId,
            Long itemId
    ) {

        ProductIssue issue = findById(issueId);

        validateDraft(issue);

        ProductIssueItem item =
                itemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Không tìm thấy dòng phiếu xuất."
                                )
                        );

        if (!item.getIssue().getId().equals(issueId)) {
            throw new BusinessException(
                    "Dòng phiếu xuất không thuộc phiếu này."
            );
        }

        itemRepository.delete(item);

        updateTotal(issue);
    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "ProductIssue"
    )
    @Transactional
    public ProductIssueResponse update(
            Long id,
            ProductIssueRequest request
    ) {

        ProductIssue issue = findById(id);

        validateDraft(issue);

        Warehouse warehouse =
                warehouseService.findWarehouseById(
                        request.getWarehouseId()
                );

        Customer customer =
                request.getCustomerId() != null
                        ? customerService.findCustomerById(
                                request.getCustomerId()
                        )
                        : null;

        issue.setWarehouse(warehouse);
        issue.setCustomer(customer);
        issue.setIssueDate(request.getIssueDate());

        repository.save(issue);

        return map(issue);
    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "ProductIssue"
    )
    @Transactional
    public void delete(Long id) {

        ProductIssue issue = findById(id);

        validateDraft(issue);

        repository.delete(issue);
    }

    @Audit(
            action = AuditAction.CONFIRM,
            entity = "ProductIssue"
    )
    @Transactional
    public void confirm(Long id) {

        ProductIssue issue = findById(id);

        validateDraft(issue);

        List<ProductIssueItem> items =
                itemRepository.findByIssueId(id);

        if (items.isEmpty()) {
            throw new BusinessException(
                    "Phiếu xuất chưa có dòng hàng nào."
            );
        }

        for (ProductIssueItem item : items) {

            ProductInventory inventory =
                    findInventory(issue, item);

            if (inventory.getQuantity()
                    .compareTo(item.getQuantity()) < 0) {

                throw new BusinessException(
                        String.format(
                                "Tồn kho sản phẩm %s không đủ. Có sẵn: %s, yêu cầu: %s",
                                item.getProduct().getName(),
                                inventory.getQuantity(),
                                item.getQuantity()
                        )
                );
            }
        }

        for (ProductIssueItem item : items) {

            ProductInventory inventory =
                    findInventory(issue, item);

            inventory.setQuantity(
                    inventory.getQuantity()
                            .subtract(item.getQuantity())
            );

            inventoryRepository.save(inventory);
        }

        issue.setStatus(IssueStatus.CONFIRMED);

        repository.save(issue);

        items.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .forEach(productService::recalculateAveragePrice);
    }

    public ProductIssue findById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy phiếu xuất sản phẩm."
                        )
                );
    }

    private ProductInventory findInventory(
            ProductIssue issue,
            ProductIssueItem item
    ) {

        return inventoryRepository
                .findByWarehouseIdAndProductId(
                        issue.getWarehouse().getId(),
                        item.getProduct().getId()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                String.format(
                                        "Không tìm thấy tồn kho sản phẩm %s trong kho.",
                                        item.getProduct().getName()
                                )
                        )
                );
    }

    private void updateTotal(ProductIssue issue) {

        BigDecimal total =
                itemRepository.findByIssueId(issue.getId())
                        .stream()
                        .map(ProductIssueItem::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        issue.setTotalAmount(total);

        repository.save(issue);
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

    private void validateDraft(ProductIssue issue) {

        if (issue.getStatus() != IssueStatus.DRAFT) {
            throw new BusinessException(
                    "Chỉ có thể chỉnh sửa phiếu xuất ở trạng thái nháp."
            );
        }
    }

    private ProductIssueResponse map(
            ProductIssue issue
    ) {

        return ProductIssueResponse.builder()
                .id(issue.getId())
                .issueNo(issue.getIssueNo())
                .warehouse(issue.getWarehouse().getName())
                .customer(
                        issue.getCustomer() != null
                                ? issue.getCustomer().getName()
                                : null
                )
                .issueDate(issue.getIssueDate())
                .status(issue.getStatus())
                .totalAmount(issue.getTotalAmount())
                .build();
    }

    private ProductIssueItemResponse map(
            ProductIssueItem item
    ) {

        return ProductIssueItemResponse.builder()
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