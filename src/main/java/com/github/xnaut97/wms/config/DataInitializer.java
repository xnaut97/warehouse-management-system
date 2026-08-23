package com.github.xnaut97.wms.config;

import com.github.xnaut97.wms.seed.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
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
    private final ProductSeeder productSeeder;
    private final BOMSeeder bomSeeder;
    private final MaterialStockScenarioSeeder materialStockScenarioSeeder;
    private final IssueScenarioSeeder issueScenarioSeeder;
    private final ProductStockScenarioSeeder productStockScenarioSeeder;
    private final ProductIssueScenarioSeeder productIssueScenarioSeeder;
    private final StocktakingVarianceSeeder stocktakingVarianceSeeder;

    @Override
    public void run(String @NonNull ... args) {

        roleSeeder.seed();
        userSeeder.seed();

        warehouseSeeder.seed();
        supplierSeeder.seed();
        customerSeeder.seed();
        materialSeeder.seed();

        seedSampleData("product", productSeeder::seed);

        seedSampleData("inventory", inventorySeeder::seed);

        seedSampleData("receipt", receiptSeeder::seed);
        seedSampleData("issue", issueSeeder::seed);
        seedSampleData("stocktaking", stocktakingSeeder::seed);

        seedSampleData("bom", bomSeeder::seed);

        seedSampleData(
                "material stock scenario",
                materialStockScenarioSeeder::seed
        );

        seedSampleData(
                "issue scenario",
                issueScenarioSeeder::seed
        );

        seedSampleData(
                "product stock scenario",
                productStockScenarioSeeder::seed
        );

        seedSampleData(
                "product issue scenario",
                productIssueScenarioSeeder::seed
        );

        seedSampleData(
                "stocktaking variance scenario",
                stocktakingVarianceSeeder::seed
        );
    }

    private void seedSampleData(String name, Runnable seeder) {

        try {
            seeder.run();
        } catch (RuntimeException ex) {
            log.error(
                    "Sample data seeding failed for '{}'; continuing application startup.",
                    name,
                    ex
            );
        }

    }
}
