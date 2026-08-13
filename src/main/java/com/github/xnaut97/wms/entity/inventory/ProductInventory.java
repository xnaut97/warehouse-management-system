package com.github.xnaut97.wms.entity.inventory;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "product_inventories",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "warehouse_id",
                "product_id",
                "lot_number"
            }
        )
    }
)
@Getter
@Setter
public class ProductInventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    private String lotNumber;

    private LocalDate expirationDate;
}