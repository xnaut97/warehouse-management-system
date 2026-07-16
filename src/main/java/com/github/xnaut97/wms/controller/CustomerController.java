package com.github.xnaut97.wms.controller;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.customer.CustomerRequest;
import com.github.xnaut97.wms.dto.customer.CustomerResponse;
import com.github.xnaut97.wms.dto.customer.UpdateCustomerRequest;
import com.github.xnaut97.wms.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<CustomerResponse> create(
            @RequestBody @Valid CustomerRequest request
    ) {

        return ApiResponse.success(
                "Customer created successfully",
                service.create(request)
        );

    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','EXECUTIVE_BOARD')")
    public ApiResponse<Page<CustomerResponse>> getAll(
            Pageable pageable
    ) {

        return ApiResponse.success(
                "Customers retrieved successfully",
                service.getAll(pageable)
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','EXECUTIVE_BOARD')")
    public ApiResponse<CustomerResponse> getById(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "Customer retrieved successfully",
                service.getById(id)
        );

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<CustomerResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCustomerRequest request
    ) {

        return ApiResponse.success(
                "Customer updated successfully",
                service.update(id, request)
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ) {

        service.delete(id);

        return ApiResponse.success(
                "Customer deleted successfully"
        );

    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> enable(
            @PathVariable Long id
    ) {

        service.enable(id);

        return ApiResponse.success(
                "Customer enabled successfully"
        );

    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> disable(
            @PathVariable Long id
    ) {

        service.disable(id);

        return ApiResponse.success(
                "Customer disabled successfully"
        );

    }

}