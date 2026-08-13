package com.github.xnaut97.wms.controller.product;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.product.ProductRequest;
import com.github.xnaut97.wms.dto.product.ProductResponse;
import com.github.xnaut97.wms.dto.product.UpdateProductRequest;
import com.github.xnaut97.wms.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<ProductResponse> create(

            @RequestBody
            @Valid
            ProductRequest request

    ) {

        return ApiResponse.success(

                "Finished product created successfully",

                service.create(request)

        );

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<ProductResponse> update(

            @PathVariable
            Long id,

            @RequestBody
            @Valid
            UpdateProductRequest request

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
    public ApiResponse<ProductResponse> getById(

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
    public ApiResponse<Page<ProductResponse>> getAll(

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
    public ApiResponse<Page<ProductResponse>> search(

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