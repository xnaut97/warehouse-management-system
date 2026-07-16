package com.github.xnaut97.wms.entity.user;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.enums.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType role;

    private String description;

}