package com.github.xnaut97.wms.entity.bom;

import com.github.xnaut97.wms.entity.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boms")
@Getter
@Setter
@NoArgsConstructor
public class BOM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Boolean enabled = true;

    @OneToMany(
            mappedBy = "bom",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BOMItem> items = new ArrayList<>();
}