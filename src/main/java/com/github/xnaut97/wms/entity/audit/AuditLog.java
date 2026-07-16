package com.github.xnaut97.wms.entity.audit;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.enums.AuditAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(nullable = false)
    private String entityName;

    @Column
    private Long entityId;

    @Column(nullable = false)
    private String username;

    @Column(length = 1000)
    private String description;

    @Column
    private String ipAddress;

}