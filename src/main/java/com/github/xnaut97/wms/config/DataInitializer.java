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

    @Override
    public void run(String @NonNull ... args) {

        roleSeeder.seed();
        userSeeder.seed();

        warehouseSeeder.seed();
        supplierSeeder.seed();
        customerSeeder.seed();
        materialSeeder.seed();

        seedSampleData("inventory", inventorySeeder::seed);

        seedSampleData("receipt", receiptSeeder::seed);
        seedSampleData("issue", issueSeeder::seed);
        seedSampleData("stocktaking", stocktakingSeeder::seed);
        seedSampleData("product", productSeeder::seed);
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
