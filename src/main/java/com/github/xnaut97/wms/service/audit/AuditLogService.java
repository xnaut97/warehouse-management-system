package com.github.xnaut97.wms.service.audit;

import com.github.xnaut97.wms.dto.audit.AuditLogResponse;
import com.github.xnaut97.wms.entity.audit.AuditLog;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.repository.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    public void save(

            AuditAction action,

            String entity,

            Long entityId,

            String username,

            String description,

            String ip

    ) {

        AuditLog log = new AuditLog();

        log.setAction(action);

        log.setEntityName(entity);

        log.setEntityId(entityId);

        log.setUsername(username);

        log.setDescription(description);

        log.setIpAddress(ip);

        repository.save(log);

    }

    @Transactional
    public Page<AuditLogResponse> getAuditLogs(

            String username,

            AuditAction action,

            LocalDate fromDate,

            LocalDate toDate,

            Pageable pageable

    ) {

        return repository.search(

                username,

                action,

                fromDate == null ? null : fromDate.atStartOfDay(),

                toDate == null ? null : toDate.atTime(23, 59, 59),

                pageable

        );

    }
}
