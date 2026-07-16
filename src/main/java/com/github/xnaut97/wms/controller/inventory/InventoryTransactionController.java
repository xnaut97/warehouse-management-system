package com.github.xnaut97.wms.controller.inventory;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.inventory.InventoryTransactionResponse;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.service.inventory.InventoryTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final InventoryTransactionService service;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'WAREHOUSE_MANAGER',
                'WAREHOUSE_STAFF',
                'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<Page<InventoryTransactionResponse>> getAll(

            @RequestParam(required = false)
            Long warehouseId,

            @RequestParam(required = false)
            Long materialId,

            @RequestParam(required = false)
            InventoryTransactionType type,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate,

            Pageable pageable

    ){

        return ApiResponse.success(

                "Inventory transactions retrieved successfully",

                service.getAll(

                        warehouseId,

                        materialId,

                        type,

                        fromDate,

                        toDate,

                        pageable

                )

        );

    }

}