package com.github.xnaut97.wms.entity.product;

import com.github.xnaut97.wms.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "product_issue_items")
@Getter
@Setter
public class ProductIssueItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ProductIssue issue;

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
