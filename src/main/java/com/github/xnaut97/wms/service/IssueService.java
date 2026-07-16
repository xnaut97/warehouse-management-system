package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.issue.*;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.entity.goods.GoodsIssueItem;
import com.github.xnaut97.wms.entity.inventory.Inventory;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.goods.GoodsIssueItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
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
public class IssueService {

    private final GoodsIssueRepository repository;

    private final GoodsIssueItemRepository itemRepository;

    private final WarehouseService warehouseService;

    private final CustomerService customerService;

    private final RawMaterialService materialService;

    private final UserService userService;

    private final DocumentNumberService generator;

    private final InventoryRepository inventoryRepository;

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
                warehouseService.findWarehouseById(
                        request.getWarehouseId());

        Customer customer =
                customerService.findCustomerById(
                        request.getCustomerId());

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

        if(itemRepository.existsByIssueIdAndMaterialId(
                issueId,
                request.getMaterialId())){

            throw new BusinessException(
                    "Material already exists in this issue."
            );

        }

        RawMaterial material =
                materialService.findMaterialById(
                        request.getMaterialId());

        GoodsIssueItem item = new GoodsIssueItem();

        item.setIssue(issue);

        item.setMaterial(material);

        item.setQuantity(request.getQuantity());

        item.setUnitPrice(request.getUnitPrice());

        item.setAmount(

                request.getQuantity()
                        .multiply(request.getUnitPrice())

        );

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
                .warehouse(issue.getWarehouse().getName())
                .customer(issue.getCustomer().getName())
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
                                        "Issue item not found."
                                ));

        item.setQuantity(request.getQuantity());

        item.setUnitPrice(request.getUnitPrice());

        item.setAmount(

                request.getQuantity()
                        .multiply(request.getUnitPrice())

        );

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
                                        "Issue item not found."
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

        if(items.isEmpty()){

            throw new BusinessException(
                    "Issue has no items."
            );

        }

        for(GoodsIssueItem item : items){

            validateInventory(issue,item);

            deductInventory(issue,item);

            createInventoryTransaction(issue,item);

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
                warehouseService.findWarehouseById(
                        request.getWarehouseId());

        Customer customer =
                customerService.findCustomerById(
                        request.getCustomerId());

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
    ){
        Inventory inventory = inventoryRepository
                        .findByWarehouseIdAndMaterialId(
                                issue.getWarehouse().getId(),
                                item.getMaterial().getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        String.format("No inventory found for warehouse %d, material %d",
                                                issue.getWarehouse().getId(),
                                                item.getMaterial().getId())
                                ));

        if (inventory.getQuantity().compareTo(item.getQuantity()) < 0) {

            throw new BusinessException(
                    String.format(
                            "Insufficient inventory id %d for %s. Available: %s, Requested: %s",
                            inventory.getId(),
                            item.getMaterial().getName(),
                            inventory.getQuantity(),
                            item.getQuantity()
                    )
            );

        }
    }

    private void deductInventory(
            GoodsIssue issue,
            GoodsIssueItem item
    ){

        Inventory inventory =
                inventoryRepository
                        .findByWarehouseIdAndMaterialId(
                                issue.getWarehouse().getId(),
                                item.getMaterial().getId()
                        )
                        .orElseThrow();

        inventory.setQuantity(

                inventory.getQuantity()

                        .subtract(item.getQuantity())

        );

        inventoryRepository.save(inventory);

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
                                "Issue not found."
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
                    "Only draft issues can be modified."
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
                .customer(issue.getCustomer().getName())
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
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        issue.setTotalAmount(total);

        repository.save(issue);

    }
}