package com.github.xnaut97.wms.entity.product;

import com.github.xnaut97.wms.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String specification;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal sellingPrice;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal averagePrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal minimumStock = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal maximumStock = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean enabled = true;

}
