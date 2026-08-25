package com.github.xnaut97.wms.controller.bom;

import com.github.xnaut97.wms.dto.bom.BOMRequest;
import com.github.xnaut97.wms.dto.bom.BOMResponse;
import com.github.xnaut97.wms.dto.bom.UpdateBOMRequest;
import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.service.bom.BOMService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BOMController {

    private final BOMService service;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF')")
    public ApiResponse<List<BOMResponse>> getAll() {

        return ApiResponse.success(
                "BOMs retrieved successfully",
                service.getAll()
        );

    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF')")
    public ApiResponse<BOMResponse> getById(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "BOM retrieved successfully",
                service.getById(id)
        );

    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<BOMResponse> create(
            @RequestBody
            @Valid
            BOMRequest request
    ) {

        return ApiResponse.success(
                "BOM created successfully",
                service.create(request)
        );

    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<BOMResponse> update(
            @PathVariable Long id,
            @RequestBody
            @Valid
            UpdateBOMRequest request
    ) {

        return ApiResponse.success(
                "BOM updated successfully",
                service.update(id, request)
        );

    }


    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<BOMResponse> disable(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "BOM disabled successfully",
                service.disable(id)
        );

    }


    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<BOMResponse> enable(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "BOM enabled successfully",
                service.enable(id)
        );

    }


    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF')")
    public ApiResponse<List<BOMResponse>> search(
            @RequestParam String keyword
    ) {

        return ApiResponse.success(
                "BOMs retrieved successfully",
                service.search(keyword)
        );

    }

}