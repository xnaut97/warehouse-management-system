package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.dto.statistic.StatisticResponse;
import com.github.xnaut97.wms.repository.*;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final CustomerRepository customerRepository;
    private final InventoryRepository inventoryRepository;
    private final GoodsIssueRepository issueRepository;
    private final GoodsReceiptRepository receiptRepository;
    private final StocktakingRepository stocktakingRepository;
    private final UserRepository userRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final RawMaterialRepository materialRepository;

    public StatisticResponse getStatistics() {
        return StatisticResponse.builder()
                .customers(customerRepository.count())
                .inventories(inventoryRepository.count())
                .issues(issueRepository.count())
                .receipts(receiptRepository.count())
                .stocktaking(stocktakingRepository.count())
                .users(userRepository.count())
                .products(finishedProductRepository.count())
                .warehouses(warehouseRepository.count())
                .suppliers(supplierRepository.count())
                .materials(materialRepository.count())
                .build();
    }
}
