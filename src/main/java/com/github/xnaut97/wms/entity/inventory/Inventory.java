package com.github.xnaut97.wms.entity.inventory;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.entity.common.Warehouse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
        name = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "warehouse_id",
                                "material_id"
                        }
                )
        }
)
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private RawMaterial material;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

}