package com.github.xnaut97.wms.entity.material;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.enums.SupplierGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32)")
    private SupplierGroup supplierGroup;

    private String contactPerson;

    private String phone;

    private String email;

    private String address;

    @Column(length = 1000)
    private String note;

}
