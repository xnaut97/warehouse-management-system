package com.github.xnaut97.wms.repository.stocktaking;

import com.github.xnaut97.wms.dto.dashboard.HighVarianceMaterialResponse;
import com.github.xnaut97.wms.dto.dashboard.TopVarianceItemResponse;
import com.github.xnaut97.wms.entity.stock.StocktakingItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface StocktakingItemRepository
        extends JpaRepository<StocktakingItem, Long> {

    List<StocktakingItem> findByStocktakingId(Long stocktakingId);

    boolean existsByStocktakingIdAndMaterialId(
            Long stocktakingId,
            Long materialId
    );

    @Query("""
            SELECT COALESCE(SUM(i.varianceQuantity),0)
            FROM StocktakingItem i
            """)
    BigDecimal getTotalVarianceQuantity();

    @Query("""
            SELECT COALESCE(SUM(i.varianceQuantity * i.material.unitPrice),0)
            FROM StocktakingItem i
            """)
    BigDecimal getTotalVarianceValue();

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.TopVarianceItemResponse(
            i.material.code,
            i.material.name,
            COALESCE(SUM(i.varianceQuantity),0),
            COALESCE(SUM(i.varianceQuantity * i.material.unitPrice),0)
            )
            FROM StocktakingItem i
            GROUP BY i.material.id,
                     i.material.code,
                     i.material.name
            ORDER BY COALESCE(SUM(i.varianceQuantity),0) DESC
            """)
    List<TopVarianceItemResponse> findTopVarianceItems(

            Pageable pageable

    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.HighVarianceMaterialResponse(
            i.material.code,
            i.material.name,
            COALESCE(SUM(i.varianceQuantity),0),
            COALESCE(SUM(i.varianceQuantity * i.material.unitPrice),0)
            )
            FROM StocktakingItem i
            GROUP BY i.material.id,
                     i.material.code,
                     i.material.name
            ORDER BY ABS(COALESCE(SUM(i.varianceQuantity),0)) DESC
            """)
    List<HighVarianceMaterialResponse> findHighVarianceMaterials(

            Pageable pageable

    );

}
