package com.github.xnaut97.wms.controller.warehouse;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.common.PageResponse;
import com.github.xnaut97.wms.dto.material.MaterialRequest;
import com.github.xnaut97.wms.dto.material.MaterialResponse;
import com.github.xnaut97.wms.service.warehouse.RawMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final RawMaterialService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF')")
    public ApiResponse<PageResponse<MaterialResponse>> getAll(
            Pageable pageable
    ) {

        return ApiResponse.success(
                "Materials retrieved successfully",
                service.getAll(pageable)
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF')")
    public ApiResponse<MaterialResponse> getById(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "Material retrieved successfully",
                service.getById(id)
        );

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<MaterialResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid MaterialRequest request
    ) {

        return ApiResponse.success(
                "Material updated successfully",
                service.update(id, request)
        );

    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<MaterialResponse> disable(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "Material disabled successfully",
                service.disable(id)
        );

    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<MaterialResponse> enable(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "Material enabled successfully",
                service.enable(id)
        );

    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF')")
    public ApiResponse<PageResponse<MaterialResponse>> search(
            @RequestParam String keyword,
            Pageable pageable
    ) {

        return ApiResponse.success(
                "Materials retrieved successfully",
                service.search(keyword, pageable)
        );

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<MaterialResponse> create(
            @RequestBody @Valid MaterialRequest request
    ) {

        return ApiResponse.success(
                "Material created successfully",
                service.create(request)
        );

    }

}