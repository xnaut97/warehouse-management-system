package com.github.xnaut97.wms.controller.warehouse;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.warehouse.UpdateWarehouseRequest;
import com.github.xnaut97.wms.dto.warehouse.WarehouseRequest;
import com.github.xnaut97.wms.dto.warehouse.WarehouseResponse;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<WarehouseResponse>> getAll(

            @RequestParam(defaultValue = "")
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ){

        return ApiResponse.success(
                "Warehouses retrieved successfully",
                service.getAll(keyword,page,size)

        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<WarehouseResponse> getById(
            @PathVariable Long id
    ){

        return ApiResponse.success(
                "Warehouse retrieved successfully",
                service.getById(id)
        );

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<WarehouseResponse> create(
            @RequestBody @Valid WarehouseRequest request
    ){

        return ApiResponse.success(
                "Warehouse created successfully",
                service.create(request)
        );

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<WarehouseResponse> update(

            @PathVariable Long id,

            @RequestBody
            @Valid
            UpdateWarehouseRequest request

    ){

        return ApiResponse.success(

                "Warehouse updated successfully",

                service.update(id, request)

        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ){

        service.delete(id);

        return ApiResponse.success(
                "Warehouse deleted successfully"
        );

    }

}