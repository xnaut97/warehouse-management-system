package com.github.xnaut97.wms.config;

import com.github.xnaut97.wms.seed.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleSeeder roleSeeder;
    private final UserSeeder userSeeder;
    private final WarehouseSeeder warehouseSeeder;
    private final SupplierSeeder supplierSeeder;
    private final CustomerSeeder customerSeeder;
    private final MaterialSeeder materialSeeder;
    private final InventorySeeder inventorySeeder;
    private final ReceiptSeeder receiptSeeder;
    private final IssueSeeder issueSeeder;
    private final StocktakingSeeder stocktakingSeeder;
    private final FinishedProductSeeder finishedProductSeeder;

    @Override
    public void run(String @NonNull ... args) {

        roleSeeder.seed();
        userSeeder.seed();

        warehouseSeeder.seed();
        supplierSeeder.seed();
        customerSeeder.seed();
        materialSeeder.seed();

        inventorySeeder.seed();

        receiptSeeder.seed();
        issueSeeder.seed();
        stocktakingSeeder.seed();
        finishedProductSeeder.seed();
    }
}