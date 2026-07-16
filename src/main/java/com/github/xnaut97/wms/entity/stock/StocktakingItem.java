package com.github.xnaut97.wms.entity.stock;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class StocktakingItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Stocktaking stocktaking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private RawMaterial material;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal systemQuantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal physicalQuantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal varianceQuantity;

}