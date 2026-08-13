package com.github.xnaut97.wms.entity.stock;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "stocktaking_item_batches")
public class StocktakingItemBatch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private StocktakingItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ProductInventory productInventory;

    private String lotNumber;

    private LocalDate expirationDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal systemQuantity;

    @Column(precision = 18, scale = 2)
    private BigDecimal physicalQuantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal varianceQuantity;

    @Column(length = 500)
    private String reason;

}
