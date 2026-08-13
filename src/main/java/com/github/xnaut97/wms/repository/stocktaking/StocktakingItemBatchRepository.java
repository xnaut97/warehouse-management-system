package com.github.xnaut97.wms.repository.stocktaking;

import com.github.xnaut97.wms.entity.stock.StocktakingItemBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StocktakingItemBatchRepository
        extends JpaRepository<StocktakingItemBatch, Long> {

    List<StocktakingItemBatch> findByItemId(Long itemId);

    List<StocktakingItemBatch> findByItemStocktakingId(Long stocktakingId);

}
