package com.github.xnaut97.wms.dto.statistic;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatisticResponse {

    private long customers;

    private long inventories;

    private long issues;

    private long materials;

    private long products;

    private long receipts;

    private long stocktaking;

    private long suppliers;

    private long users;

    private long warehouses;

}
