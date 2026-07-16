package com.github.xnaut97.wms.controller.stock;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.stocktaking.*;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import com.github.xnaut97.wms.service.stock.StocktakingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stocktaking")
@RequiredArgsConstructor
public class StocktakingController {

    private final StocktakingService service;

    @PostMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'WAREHOUSE_MANAGER'
            )
            """)
    public ApiResponse<StocktakingResponse> create(

            @RequestBody
            @Valid
            StocktakingRequest request

    ) {

        return ApiResponse.success(

                "Stocktaking created successfully",

                service.create(request)

        );

    }

    @PostMapping("/{id}/items")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER'
            )
            """)
    public ApiResponse<StocktakingItemResponse> addItem(

            @PathVariable Long id,

            @RequestBody
            @Valid
            AddStocktakingItemRequest request

    ) {

        return ApiResponse.success(

                "Item added successfully",

                service.addItem(id, request)

        );

    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER'
            )
            """)
    public ApiResponse<StocktakingItemResponse> updateItem(

            @PathVariable Long itemId,

            @RequestBody
            @Valid
            UpdateStocktakingItemRequest request

    ) {

        return ApiResponse.success(

                "Item updated successfully",

                service.updateItem(itemId, request)

        );

    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER'
            )
            """)
    public ApiResponse<Void> confirm(

            @PathVariable Long id

    ) {

        service.confirm(id);

        return ApiResponse.success(
                "Stocktaking confirmed successfully"
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'WAREHOUSE_STAFF',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<StocktakingResponse> get(

            @PathVariable Long id

    ) {

        return ApiResponse.success(

                "Stocktaking retrieved successfully",

                service.getById(id)

        );

    }

    @GetMapping
    @PreAuthorize("""
hasAnyRole(
'ADMIN',
'WAREHOUSE_MANAGER',
'WAREHOUSE_STAFF',
'EXECUTIVE_BOARD'
)
""")
    public ApiResponse<Page<StocktakingResponse>> getAll(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Long warehouseId,

            @RequestParam(required = false)
            StocktakingStatus status,

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

    ) {

        return ApiResponse.success(

                "Stocktaking list retrieved successfully",

                service.getAll(

                        keyword,

                        warehouseId,

                        status,

                        fromDate,

                        toDate,

                        pageable

                )

        );

    }
}