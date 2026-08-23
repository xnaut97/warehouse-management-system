package com.github.xnaut97.wms.repository.stocktaking;

import com.github.xnaut97.wms.entity.stock.Stocktaking;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StocktakingRepository extends
        JpaRepository<Stocktaking, Long>,
        JpaSpecificationExecutor<Stocktaking> {

    boolean existsByWarehouseId(Long warehouseId);

    boolean existsByWarehouseIdAndStatus(
            Long warehouseId,
            StocktakingStatus status
    );

}
