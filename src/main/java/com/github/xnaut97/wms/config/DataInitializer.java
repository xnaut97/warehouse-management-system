package com.github.xnaut97.wms.config;

import com.github.xnaut97.wms.seed.BOMSeeder;
import com.github.xnaut97.wms.seed.CustomerSeeder;
import com.github.xnaut97.wms.seed.MaterialSeeder;
import com.github.xnaut97.wms.seed.ProductSeeder;
import com.github.xnaut97.wms.seed.ProductionOperationSeeder;
import com.github.xnaut97.wms.seed.RoleSeeder;
import com.github.xnaut97.wms.seed.SupplierSeeder;
import com.github.xnaut97.wms.seed.UserSeeder;
import com.github.xnaut97.wms.seed.WarehouseSeeder;
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
    private final ProductSeeder productSeeder;
    private final BOMSeeder bomSeeder;
    private final ProductionOperationSeeder productionOperationSeeder;

    @Override
    public void run(String @NonNull ... args) {

        roleSeeder.seed();
        userSeeder.seed();
        warehouseSeeder.seed();
        supplierSeeder.seed();
        customerSeeder.seed();
        materialSeeder.seed();
        productSeeder.seed();
        bomSeeder.seed();
        productionOperationSeeder.seed();
    }

}
