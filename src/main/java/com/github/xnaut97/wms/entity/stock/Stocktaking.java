package com.github.xnaut97.wms.entity.stock;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Stocktaking extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String stocktakingNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private LocalDate stocktakingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StocktakingStatus status;

    @Column(length = 1000)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User createdBy;

}