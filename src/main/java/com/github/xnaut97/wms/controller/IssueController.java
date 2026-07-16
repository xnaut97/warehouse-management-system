package com.github.xnaut97.wms.controller;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.issue.*;
import com.github.xnaut97.wms.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService service;

    @GetMapping("/{issueId}/items")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','EXECUTIVE_BOARD')")
    public ApiResponse<List<IssueItemResponse>> getItems(
            @PathVariable Long issueId
    ) {

        return ApiResponse.success(
                "Items retrieved successfully",
                service.getItems(issueId)
        );

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<IssueResponse> create(
            @RequestBody @Valid IssueRequest request
    ) {
        return ApiResponse.success(
                "Created successfully",
                service.create(request));
    }

    @PostMapping("/{issueId}/items")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<IssueItemResponse> addItem(
            @PathVariable Long issueId,
            @RequestBody @Valid AddIssueItemRequest request
    ) {

        return ApiResponse.success(
                "Item added successfully",
                service.addItem(issueId, request)
        );

    }


    @PutMapping("/{issueId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<IssueItemResponse> updateItem(
            @PathVariable Long issueId,
            @PathVariable Long itemId,
            @RequestBody @Valid UpdateIssueItemRequest request
    ) {

        return ApiResponse.success(
                "Item updated successfully",
                service.updateItem(
                        issueId,
                        itemId,
                        request
                )
        );

    }

    @DeleteMapping("/{issueId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> deleteItem(
            @PathVariable Long issueId,
            @PathVariable Long itemId
    ) {

        service.deleteItem(
                issueId,
                itemId
        );

        return ApiResponse.success(
                "Item deleted successfully"
        );

    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> confirm(
            @PathVariable Long id
    ){

        service.confirm(id);

        return ApiResponse.success(
                "Issue confirmed successfully"
        );

    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','EXECUTIVE_BOARD')")
    public ApiResponse<Page<IssueResponse>> getAll(

            Pageable pageable

    ) {

        return ApiResponse.success(

                "Issues retrieved successfully",

                service.getAll(pageable)

        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','EXECUTIVE_BOARD')")
    public ApiResponse<IssueDetailResponse> getDetail(

            @PathVariable
            Long id

    ) {

        return ApiResponse.success(

                "Issue retrieved successfully",

                service.getDetail(id)

        );

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<IssueResponse> update(

            @PathVariable
            Long id,

            @RequestBody
            @Valid
            IssueRequest request

    ) {

        return ApiResponse.success(

                "Issue updated successfully",

                service.update(
                        id,
                        request
                )

        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public ApiResponse<Void> delete(

            @PathVariable
            Long id

    ) {

        service.delete(id);

        return ApiResponse.success(
                "Issue deleted successfully"
        );

    }

}