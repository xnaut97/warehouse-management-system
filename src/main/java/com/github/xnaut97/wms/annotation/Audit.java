package com.github.xnaut97.wms.annotation;

import com.github.xnaut97.wms.enums.AuditAction;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {

    AuditAction action();

    String entity();

}