package com.github.xnaut97.wms.dto.audit;

import com.github.xnaut97.wms.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;

    private AuditAction action;

    private String entityName;

    private Long entityId;

    private String username;

    private String description;

    private String ipAddress;

    private LocalDateTime createdAt;

}