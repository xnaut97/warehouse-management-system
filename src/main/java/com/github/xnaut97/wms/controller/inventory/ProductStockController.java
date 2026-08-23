package com.github.xnaut97.wms.controller.inventory;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.inventory.ProductStockResponse;
import com.github.xnaut97.wms.service.inventory.ProductStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product-inventories")
@RequiredArgsConstructor
public class ProductStockController {

    private final ProductStockService service;

    @GetMapping("/lots")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','WAREHOUSE_STAFF','EXECUTIVE_BOARD','ACCOUNTANT')")
    public ApiResponse<List<ProductStockResponse>> getAvailableLots(

            @RequestParam
            Long warehouseId,

            @RequestParam(required = false)
            Long productId

    ) {

        return ApiResponse.success(
                "Product stock retrieved successfully",
                service.getAvailableLots(
                        warehouseId,
                        productId
                )
        );

    }

}
