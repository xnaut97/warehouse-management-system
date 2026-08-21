package com.github.xnaut97.wms.entity.common;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.enums.CustomerGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32)")
    private CustomerGroup customerGroup;

    private String receiverName;

    private String phone;

    @Column(unique = true)
    private String email;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    private Boolean enabled = true;

}
