package com.github.xnaut97.wms.entity.goods;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.material.Material;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name="goods_issue_items")
@Getter
@Setter
public class GoodsIssueItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private GoodsIssue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Material material;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column
    private BigDecimal unitPrice;

    @Column
    private BigDecimal amount;

}