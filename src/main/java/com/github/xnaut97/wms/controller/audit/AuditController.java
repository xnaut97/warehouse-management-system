package com.github.xnaut97.wms.controller.audit;

import com.github.xnaut97.wms.dto.audit.AuditLogResponse;
import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogService service;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<AuditLogResponse>> getAuditLogs(

            @RequestParam(required = false)
            String username,

            @RequestParam(required = false)
            AuditAction action,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate

    ) {

        return ApiResponse.success(

                "Audit logs retrieved successfully",

                service.getAuditLogs(

                        username,

                        action,

                        fromDate,

                        toDate

                )

        );

    }

}