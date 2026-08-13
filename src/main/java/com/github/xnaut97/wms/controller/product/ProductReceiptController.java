package com.github.xnaut97.wms.controller.product;

import com.github.xnaut97.wms.dto.product.receipt.*;
import com.github.xnaut97.wms.service.product.ProductReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-receipts")
@RequiredArgsConstructor
public class ProductReceiptController {

    private final ProductReceiptService service;

    @GetMapping
    public ResponseEntity<Page<ProductReceiptResponse>> getAll(
            @PageableDefault(size = 8)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                service.getAll(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductReceiptDetailResponse> getDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.getDetail(id)
        );
    }

    @PostMapping
    public ResponseEntity<ProductReceiptResponse> create(
            @Valid @RequestBody ProductReceiptRequest request
    ) {
        return ResponseEntity.ok(
                service.create(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductReceiptResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductReceiptRequest request
    ) {
        return ResponseEntity.ok(
                service.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Void> confirm(
            @PathVariable Long id
    ) {
        service.confirm(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{receiptId}/items")
    public ResponseEntity<ProductReceiptItemResponse> addItem(
            @PathVariable Long receiptId,
            @Valid @RequestBody AddProductReceiptItemRequest request
    ) {
        return ResponseEntity.ok(
                service.addItem(receiptId, request)
        );
    }

    @PutMapping("/{receiptId}/items/{itemId}")
    public ResponseEntity<ProductReceiptItemResponse> updateItem(
            @PathVariable Long receiptId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateProductReceiptItemRequest request
    ) {
        return ResponseEntity.ok(
                service.updateItem(
                        receiptId,
                        itemId,
                        request
                )
        );
    }

    @DeleteMapping("/{receiptId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long receiptId,
            @PathVariable Long itemId
    ) {
        service.deleteItem(receiptId, itemId);

        return ResponseEntity.noContent().build();
    }
}