package com.github.xnaut97.wms.controller.inventory;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.inventory.InventoryDetailResponse;
import com.github.xnaut97.wms.dto.inventory.InventoryResponse;
import com.github.xnaut97.wms.dto.inventory.LowStockResponse;
import com.github.xnaut97.wms.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @GetMapping("/low-stock")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'WAREHOUSE_STAFF',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<LowStockResponse>> getLowStock() {

        return ApiResponse.success(
                "Low stock retrieved successfully",
                service.getLowStock()
        );

    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF','EXECUTIVE_BOARD')")
    public ApiResponse<Page<InventoryResponse>> getAll(

            @RequestParam(required = false)
            Long warehouseId,

            @RequestParam(required = false)
            Long materialId,

            @RequestParam(required = false)
            String keyword,

            Pageable pageable

    ) {

        return ApiResponse.success(

                "Inventories retrieved successfully",

                service.getAll(

                        warehouseId,

                        materialId,

                        keyword,

                        pageable

                )

        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF','EXECUTIVE_BOARD')")
    public ApiResponse<InventoryDetailResponse> getDetail(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "Inventory retrieved successfully",
                service.getDetail(id)
        );

    }

}