package com.github.xnaut97.wms.entity.bom;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.material.Material;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
        name = "bom_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bom_material",
                        columnNames = {
                                "bom_id",
                                "raw_material_id"
                        }
                )
        }
)
public class BOMItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bom_id", nullable = false)
    private BOM bom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raw_material_id", nullable = false)
    private Material material;

    @Column(
            nullable = false,
            precision = 18,
            scale = 4
    )
    private BigDecimal consumptionQuantity;

    @Column(
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal mixingRatio;

    @Column(
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal maxWasteRatio;
}