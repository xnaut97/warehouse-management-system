package com.github.xnaut97.wms.controller;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.receipt.*;
import com.github.xnaut97.wms.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','EXECUTIVE_BOARD')")
    public ApiResponse<Page<ReceiptResponse>> getAll(
            Pageable pageable
    ) {

        return ApiResponse.success(
                "Receipts retrieved successfully",
                service.getAll(pageable)
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','EXECUTIVE_BOARD')")
    public ApiResponse<ReceiptDetailResponse> getDetail(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "Receipt retrieved successfully",
                service.getDetail(id)
        );

    }

    @PostMapping("/{receiptId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> confirm(
            @PathVariable Long receiptId
    ){

        service.confirm(receiptId);

        return ApiResponse.success(
                "Receipt confirmed successfully"
        );

    }

    @PostMapping("/{receiptId}/items")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<ReceiptItemResponse> addItem(
            @PathVariable Long receiptId,
            @RequestBody @Valid AddReceiptItemRequest request
    ){

        return ApiResponse.success(
                "Item added successfully",
                service.addItem(receiptId, request)
        );

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<ReceiptResponse> create(
            @RequestBody @Valid ReceiptRequest request
    ) {

        return ApiResponse.success(
                "Receipt created successfully",
                service.create(request)
        );

    }

    @PutMapping("/{receiptId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<ReceiptItemResponse> updateItem(
            @PathVariable Long receiptId,
            @PathVariable Long itemId,
            @RequestBody @Valid UpdateReceiptItemRequest request
    ){

        return ApiResponse.success(
                "Receipt item updated successfully",
                service.updateItem(receiptId,itemId,request)
        );

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<ReceiptResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateReceiptRequest request
    ){

        return ApiResponse.success(
                "Receipt updated successfully",
                service.update(id,request)
        );

    }

    @DeleteMapping("/{receiptId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> deleteItem(
            @PathVariable Long receiptId,
            @PathVariable Long itemId
    ){

        service.deleteItem(receiptId,itemId);

        return ApiResponse.success(
                "Receipt item deleted successfully"
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ){

        service.delete(id);

        return ApiResponse.success(
                "Receipt deleted successfully"
        );

    }

}