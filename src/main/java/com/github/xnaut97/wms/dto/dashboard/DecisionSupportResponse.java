package com.github.xnaut97.wms.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DecisionSupportResponse {

    private List<LowStockMaterialResponse> lowStockMaterials;

    private List<DashboardReplenishmentRecommendationResponse> replenishmentRecommendations;

    private List<SlowMovingMaterialResponse> slowMovingMaterials;

    private List<HighVarianceMaterialResponse> highVarianceMaterials;

    private List<DashboardInventoryTrendResponse> inventoryTrend;

}
