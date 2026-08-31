package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.issue.*;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.entity.goods.GoodsIssueItem;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.goods.GoodsIssueItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.service.user.UserService;
import com.github.xnaut97.wms.service.warehouse.MaterialService;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final GoodsIssueRepository repository;

    private final GoodsIssueItemRepository itemRepository;

    private final WarehouseService warehouseService;

    private final CustomerService customerService;

    private final MaterialService materialService;

    private final UserService userService;

    private final DocumentNumberService generator;

    private final MaterialInventoryRepository materialInventoryRepository;

    private final InventoryTransactionRepository transactionRepository;

    @Transactional
    public List<IssueItemResponse> getItems(
            Long issueId
    ) {

        findIssueById(issueId);

        return itemRepository.findByIssueId(issueId)
                .stream()
                .map(this::map)
                .toList();

    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "GoodsIssue"
    )
    @Transactional
    public IssueResponse create(IssueRequest request) {

        Warehouse warehouse =
                warehouseService.findWarehouseByIdForGroup(
                        request.getWarehouseId(),
                        StockGroup.MATERIAL);

        Customer customer = request.getCustomerId() != null
                ? customerService.findCustomerById(request.getCustomerId())
                : null;

        User currentUser = getCurrentUser();

        GoodsIssue issue = new GoodsIssue();

        issue.setIssueNo(
                generator.next(DocumentType.GOODS_ISSUE));

        issue.setWarehouse(warehouse);

        issue.setCustomer(customer);

        issue.setIssueDate(request.getIssueDate());

        issue.setStatus(IssueStatus.DRAFT);

        issue.setCreatedBy(currentUser);

        issue.setTotalAmount(BigDecimal.ZERO);

        repository.save(issue);

        return map(issue);

    }

    @Transactional
    public IssueItemResponse addItem(
            Long issueId,
            AddIssueItemRequest request
    ) {

        GoodsIssue issue = findIssueById(issueId);

        validateDraft(issue);

        Material material =
                materialService.findMaterialById(
                        request.getMaterialId());

        validateAvailableStock(
                issue,
                material,
                request.getQuantity(),
                null
        );

        BigDecimal unitPrice = request.getUnitPrice();
        BigDecimal amount = unitPrice != null
                ? request.getQuantity().multiply(unitPrice)
                : null;

        GoodsIssueItem item = new GoodsIssueItem();

        item.setIssue(issue);

        item.setMaterial(material);

        item.setQuantity(request.getQuantity());

        item.setUnitPrice(unitPrice);

        item.setAmount(amount);

        itemRepository.save(item);

        updateIssueTotal(issue);

        return map(item);

    }

    @Transactional
    public Page<IssueResponse> getAll(
            Pageable pageable
    ) {

        return repository.findAll(pageable)
                .map(this::map);

    }

    @Transactional
    public IssueDetailResponse getDetail(Long id) {

        GoodsIssue issue = findIssueById(id);

        List<IssueItemResponse> items =
                itemRepository.findByIssueId(id)
                        .stream()
                        .map(this::map)
                        .toList();

        return IssueDetailResponse.builder()
                .id(issue.getId())
                .issueNo(issue.getIssueNo())
                .warehouseId(issue.getWarehouse().getId())
                .warehouse(issue.getWarehouse().getName())
                .customer(issue.getCustomer() != null ? issue.getCustomer().getName() : null)
                .issueDate(issue.getIssueDate())
                .status(issue.getStatus())
                .totalAmount(issue.getTotalAmount())
                .items(items)
                .build();

    }

    @Transactional
    public IssueItemResponse updateItem(
            Long issueId,
            Long itemId,
            UpdateIssueItemRequest request
    ) {

        GoodsIssue issue = findIssueById(issueId);

        validateDraft(issue);

        GoodsIssueItem item =
                itemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Không tìm thấy dòng phiếu xuất."
                                ));

        if (!item.getIssue().getId().equals(issueId)) {

            throw new BusinessException(
                    "Dòng phiếu xuất không thuộc phiếu này."
            );

        }

        validateAvailableStock(
                issue,
                item.getMaterial(),
                request.getQuantity(),
                item.getId()
        );

        BigDecimal unitPrice = request.getUnitPrice();
        BigDecimal amount = unitPrice != null
                ? request.getQuantity().multiply(unitPrice)
                : null;

        item.setQuantity(request.getQuantity());

        item.setUnitPrice(unitPrice);

        item.setAmount(amount);

        itemRepository.save(item);

        updateIssueTotal(issue);

        return map(item);

    }

    @Transactional
    public void deleteItem(
            Long issueId,
            Long itemId
    ) {

        GoodsIssue issue = findIssueById(issueId);

        validateDraft(issue);

        GoodsIssueItem item =
                itemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Không tìm thấy dòng phiếu xuất."
                                ));

        itemRepository.delete(item);

        updateIssueTotal(issue);

    }

    @Transactional
    public void confirm(Long issueId) {

        GoodsIssue issue =
                findIssueById(issueId);

        validateDraft(issue);

        List<GoodsIssueItem> items =
                itemRepository.findByIssueId(issueId);

        if (items.isEmpty()) {

            throw new BusinessException(
                    "Phiếu xuất chưa có dòng hàng nào."
            );

        }

        for (GoodsIssueItem item : items) {

            validateInventory(issue, item);

            deductInventory(issue, item);

            createInventoryTransaction(issue, item);

        }

        issue.setStatus(IssueStatus.CONFIRMED);

        repository.save(issue);
    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "GoodsIssue"
    )
    @Transactional
    public IssueResponse update(
            Long id,
            IssueRequest request
    ) {

        GoodsIssue issue = findIssueById(id);

        validateDraft(issue);

        Warehouse warehouse =
                warehouseService.findWarehouseByIdForGroup(
                        request.getWarehouseId(),
                        StockGroup.MATERIAL);

        Customer customer = request.getCustomerId() != null
                ? customerService.findCustomerById(request.getCustomerId())
                : null;

        issue.setWarehouse(warehouse);

        issue.setCustomer(customer);

        issue.setIssueDate(request.getIssueDate());

        repository.save(issue);

        return map(issue);

    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "GoodsIssue"
    )
    @Transactional
    public void delete(Long id) {

        GoodsIssue issue = findIssueById(id);

        validateDraft(issue);

        itemRepository.deleteAll(
                itemRepository.findByIssueId(id)
        );

        repository.delete(issue);

    }


    private void validateInventory(
            GoodsIssue issue,
            GoodsIssueItem item
    ) {
        MaterialInventory materialInventory =
                findInventory(issue, item.getMaterial());

        BigDecimal requested =
                itemRepository.findByIssueId(issue.getId())
                        .stream()
                        .filter(other ->
                                other.getMaterial().getId()
                                        .equals(item.getMaterial().getId())
                        )
                        .map(GoodsIssueItem::getQuantity)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (materialInventory.getQuantity().compareTo(requested) < 0) {

            throw new BusinessException(
                    String.format(
                            "Tồn kho id %d cho %s không đủ. Có sẵn: %s, Yêu cầu: %s",
                            materialInventory.getId(),
                            item.getMaterial().getName(),
                            materialInventory.getQuantity(),
                            requested
                    )
            );

        }
    }

    private MaterialInventory findInventory(
            GoodsIssue issue,
            Material material
    ) {

        return materialInventoryRepository
                .findByWarehouseIdAndMaterialId(
                        issue.getWarehouse().getId(),
                        material.getId()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                String.format(
                                        "Không tìm thấy tồn kho nguyên liệu %s trong kho %s.",
                                        material.getName(),
                                        issue.getWarehouse().getName()
                                )
                        ));

    }

    /**
     * Guards every write path of the issue document against the real stock of
     * the warehouse the document belongs to, netting out what the document
     * already takes from the same material.
     */
    private void validateAvailableStock(
            GoodsIssue issue,
            Material material,
            BigDecimal quantity,
            Long excludedItemId
    ) {

        MaterialInventory inventory =
                findInventory(issue, material);

        BigDecimal inStock =
                inventory.getQuantity() != null
                        ? inventory.getQuantity()
                        : BigDecimal.ZERO;

        BigDecimal available =
                inStock.subtract(
                        alreadyIssued(
                                issue,
                                material,
                                excludedItemId
                        )
                );

        if (quantity != null
                && available.compareTo(quantity) < 0) {

            throw new BusinessException(
                    String.format(
                            "Tồn kho nguyên liệu %s không đủ. Có sẵn: %s, yêu cầu: %s",
                            material.getName(),
                            available.max(BigDecimal.ZERO),
                            quantity
                    )
            );

        }

    }

    private BigDecimal alreadyIssued(
            GoodsIssue issue,
            Material material,
            Long excludedItemId
    ) {

        return itemRepository.findByIssueId(issue.getId())
                .stream()
                .filter(existing ->
                        !existing.getId().equals(excludedItemId)
                )
                .filter(existing ->
                        existing.getMaterial().getId()
                                .equals(material.getId())
                )
                .map(GoodsIssueItem::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    private void deductInventory(
            GoodsIssue issue,
            GoodsIssueItem item
    ) {

        MaterialInventory materialInventory =
                materialInventoryRepository
                        .findByWarehouseIdAndMaterialId(
                                issue.getWarehouse().getId(),
                                item.getMaterial().getId()
                        )
                        .orElseThrow();

        materialInventory.setQuantity(

                materialInventory.getQuantity()

                        .subtract(item.getQuantity())

        );

        materialInventoryRepository.save(materialInventory);

    }

    private void createInventoryTransaction(
            GoodsIssue issue,
            GoodsIssueItem item
    ) {

        InventoryTransaction transaction =
                new InventoryTransaction();

        transaction.setWarehouse(
                issue.getWarehouse()
        );

        transaction.setMaterial(
                item.getMaterial()
        );

        transaction.setQuantity(
                item.getQuantity()
        );

        transaction.setReferenceNo(
                issue.getIssueNo()
        );

        transaction.setType(
                InventoryTransactionType.OUT
        );

        transaction.setCreatedBy(
                issue.getCreatedBy()
        );

        transactionRepository.save(transaction);

    }

    public GoodsIssue findIssueById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy phiếu xuất."
                        ));

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

    private void validateDraft(
            GoodsIssue issue
    ) {

        if (issue.getStatus() != IssueStatus.DRAFT) {

            throw new BusinessException(
                    "Chỉ có thể chỉnh sửa phiếu xuất ở trạng thái nháp."
            );

        }

    }

    private IssueItemResponse map(
            GoodsIssueItem item
    ) {

        return IssueItemResponse.builder()
                .id(item.getId())
                .materialId(item.getMaterial().getId())
                .materialCode(item.getMaterial().getCode())
                .materialName(item.getMaterial().getName())
                .unit(item.getMaterial().getUnit())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .build();

    }

    private IssueResponse map(
            GoodsIssue issue
    ) {

        return IssueResponse.builder()
                .id(issue.getId())
                .issueNo(issue.getIssueNo())
                .warehouse(issue.getWarehouse().getName())
                .customer(issue.getCustomer() != null ? issue.getCustomer().getName() : null)
                .issueDate(issue.getIssueDate())
                .status(issue.getStatus())
                .totalAmount(issue.getTotalAmount())
                .build();

    }

    private void updateIssueTotal(
            GoodsIssue issue
    ) {

        BigDecimal total =
                itemRepository.findByIssueId(issue.getId())
                        .stream()
                        .map(GoodsIssueItem::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        issue.setTotalAmount(total);

        repository.save(issue);

    }
}