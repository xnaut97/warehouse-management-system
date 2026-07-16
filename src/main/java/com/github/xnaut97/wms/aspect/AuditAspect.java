package com.github.xnaut97.wms.aspect;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditService;

    @AfterReturning("@annotation(audit)")
    public void after(

            JoinPoint joinPoint,

            Audit audit

    ) {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String username;

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            username = "SYSTEM";

        } else {

            username = authentication.getName();

        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String ip = "SYSTEM";

        if (attributes != null) {
            ip = attributes.getRequest().getRemoteAddr();
        }

        auditService.save(

                audit.action(),

                audit.entity(),

                null,

                username,

                joinPoint.getSignature().getName(),

                ip

        );

    }

}