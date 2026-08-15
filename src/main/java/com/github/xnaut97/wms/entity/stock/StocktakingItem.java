package com.github.xnaut97.wms.entity.stock;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StocktakingItemStatus;
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
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    private StockGroup itemGroup;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal systemQuantity;

    @Column(precision = 18, scale = 2)
    private BigDecimal physicalQuantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal varianceQuantity;

    @Enumerated(EnumType.STRING)
    private StocktakingItemStatus itemStatus;

    @Column(length = 500)
    private String reason;

}
