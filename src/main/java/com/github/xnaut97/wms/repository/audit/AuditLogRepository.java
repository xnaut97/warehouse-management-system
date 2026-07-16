package com.github.xnaut97.wms.repository.audit;

import com.github.xnaut97.wms.dto.audit.AuditLogResponse;
import com.github.xnaut97.wms.entity.audit.AuditLog;
import com.github.xnaut97.wms.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.audit.AuditLogResponse(
            
                a.id,
            
                a.action,
            
                a.entityName,
            
                a.entityId,
            
                a.username,
            
                a.description,
            
                a.ipAddress,
            
                a.createdAt
            
            )
            
            FROM AuditLog a
            
            WHERE
            
            (:username IS NULL OR a.username = :username)
            
            AND
            
            (:action IS NULL OR a.action = :action)
            
            AND
            
            (:from IS NULL OR a.createdAt >= :from)
            
            AND
            
            (:to IS NULL OR a.createdAt <= :to)
            
            ORDER BY a.createdAt DESC
            """)
    List<AuditLogResponse> search(

            @Param("username")
            String username,

            @Param("action")
            AuditAction action,

            @Param("from")
            LocalDateTime from,

            @Param("to")
            LocalDateTime to

    );
}