package com.github.xnaut97.wms.controller;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.common.PageResponse;
import com.github.xnaut97.wms.dto.supplier.SupplierRequest;
import com.github.xnaut97.wms.dto.supplier.SupplierResponse;
import com.github.xnaut97.wms.service.SupplierService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Supplier",
        description = "Supplier Management APIs"
)
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF')")
    public ApiResponse<PageResponse<SupplierResponse>> getAll(
            Pageable pageable
    ) {

        return ApiResponse.success(
                "Suppliers retrieved successfully",
                service.getAll(pageable)
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF')")
    public ApiResponse<SupplierResponse> getById(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "Supplier retrieved successfully",
                service.getById(id)
        );

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<SupplierResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid SupplierRequest request
    ) {

        return ApiResponse.success(
                "Supplier updated successfully",
                service.update(id, request)
        );

    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF')")
    public ApiResponse<List<SupplierResponse>> search(
            @RequestParam String keyword
    ) {

        return ApiResponse.success(
                "Suppliers retrieved successfully",
                service.search(keyword)
        );

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<SupplierResponse> create(
            @RequestBody @Valid SupplierRequest request) {

        return ApiResponse.success(
                "Supplier created successfully",
                service.create(request)
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> delete(
            @RequestParam Long id) {
        service.delete(id);
        return ApiResponse.success("Suppliers deleted successfully");
    }

}