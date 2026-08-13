package com.github.xnaut97.wms.entity.product;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.IssueStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_issues")
@Getter
@Setter
public class ProductIssue extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String issueNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Customer customer;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User createdBy;

    @OneToMany(
            mappedBy = "issue",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProductIssueItem> items = new ArrayList<>();

}
