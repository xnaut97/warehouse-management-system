package com.github.xnaut97.wms.controller;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.product.FinishedProductRequest;
import com.github.xnaut97.wms.dto.product.FinishedProductResponse;
import com.github.xnaut97.wms.dto.product.UpdateFinishedProductRequest;
import com.github.xnaut97.wms.service.FinishedProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class FinishedProductController {

    private final FinishedProductService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<FinishedProductResponse> create(

            @RequestBody
            @Valid
            FinishedProductRequest request

    ) {

        return ApiResponse.success(

                "Finished product created successfully",

                service.create(request)

        );

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<FinishedProductResponse> update(

            @PathVariable
            Long id,

            @RequestBody
            @Valid
            UpdateFinishedProductRequest request

    ) {

        return ApiResponse.success(

                "Finished product updated successfully",

                service.update(id, request)

        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(

            @PathVariable
            Long id

    ) {

        service.delete(id);

        return ApiResponse.success(
                "Finished product deleted successfully"
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<FinishedProductResponse> getById(

            @PathVariable
            Long id

    ) {

        return ApiResponse.success(
                "Finished product retrieved successfully",
                service.getById(id)
        );

    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<FinishedProductResponse>> getAll(

            @RequestParam(defaultValue = "")
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ) {

        return ApiResponse.success(
                "Finished products retrieved successfully",
                service.getAll(
                        keyword,
                        page,
                        size
                )

        );

    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<FinishedProductResponse>> search(

            @RequestParam
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ) {

        return ApiResponse.success(
                "Finished products searched successfully",
                service.search(
                        keyword,
                        page,
                        size
                )
        );

    }

}