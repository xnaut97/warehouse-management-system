package com.github.xnaut97.wms.controller.product;

import com.github.xnaut97.wms.dto.product.issue.*;
import com.github.xnaut97.wms.service.product.ProductIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-issues")
@RequiredArgsConstructor
public class ProductIssueController {

    private final ProductIssueService service;

    @GetMapping
    public ResponseEntity<Page<ProductIssueResponse>> getAll(
            @PageableDefault(size = 8)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                service.getAll(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductIssueDetailResponse> getDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.getDetail(id)
        );
    }

    @PostMapping
    public ResponseEntity<ProductIssueResponse> create(
            @Valid @RequestBody ProductIssueRequest request
    ) {
        return ResponseEntity.ok(
                service.create(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductIssueResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductIssueRequest request
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

    @PostMapping("/{issueId}/items")
    public ResponseEntity<ProductIssueItemResponse> addItem(
            @PathVariable Long issueId,
            @Valid @RequestBody AddProductIssueItemRequest request
    ) {
        return ResponseEntity.ok(
                service.addItem(issueId, request)
        );
    }

    @PutMapping("/{issueId}/items/{itemId}")
    public ResponseEntity<ProductIssueItemResponse> updateItem(
            @PathVariable Long issueId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateProductIssueItemRequest request
    ) {
        return ResponseEntity.ok(
                service.updateItem(
                        issueId,
                        itemId,
                        request
                )
        );
    }

    @DeleteMapping("/{issueId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long issueId,
            @PathVariable Long itemId
    ) {
        service.deleteItem(issueId, itemId);

        return ResponseEntity.noContent().build();
    }
}