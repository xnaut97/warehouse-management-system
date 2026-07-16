package com.github.xnaut97.wms.service.inventory;

import com.github.xnaut97.wms.dto.inventory.InventoryTransactionResponse;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import com.github.xnaut97.wms.specification.InventoryTransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InventoryTransactionService {

    private final InventoryTransactionRepository repository;

    @Transactional
    public Page<InventoryTransactionResponse> getAll(
            Long warehouseId,
            Long materialId,
            InventoryTransactionType type,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable

    ) {

        return repository.findAll(
                InventoryTransactionSpecification.filter(
                        warehouseId,
                        materialId,
                        type,
                        fromDate,
                        toDate
                ),

                pageable

        ).map(this::map);

    }

    private InventoryTransactionResponse map(
            InventoryTransaction transaction
    ) {

        return InventoryTransactionResponse.builder()

                .id(transaction.getId())

                .warehouse(
                        transaction.getWarehouse().getName()
                )

                .materialCode(
                        transaction.getMaterial().getCode()
                )

                .materialName(
                        transaction.getMaterial().getName()
                )

                .type(transaction.getType())

                .quantity(transaction.getQuantity())

                .referenceNo(
                        transaction.getReferenceNo()
                )

                .createdBy(
                        transaction.getCreatedBy().getUsername()
                )

                .createdAt(
                        transaction.getCreatedAt()
                )

                .build();

    }

}