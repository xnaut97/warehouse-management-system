package com.github.xnaut97.wms.entity.product;

import com.github.xnaut97.wms.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "product_receipt_items")
public class ProductReceiptItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ProductReceipt receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal quantity;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    private String lotNumber;

    private LocalDate expirationDate;

}
