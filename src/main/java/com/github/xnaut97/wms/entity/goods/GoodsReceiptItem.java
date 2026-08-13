package com.github.xnaut97.wms.entity.goods;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.material.Material;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "goods_receipt_items")
public class GoodsReceiptItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private GoodsReceipt receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Material material;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal quantity;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    private String lotNumber;

    private LocalDate expirationDate;

}