package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class WarehouseSeeder {

    private final WarehouseRepository repository;
    private final SampleDataFactory factory;

    @Transactional
    public void seed() {
        if (repository.count() > 0) return;


        Stream.of(
                        factory.warehouse("WH001", "Main Warehouse"),
                        factory.warehouse("WH002", "North Warehouse"),
                        factory.warehouse("WH003", "South Warehouse")
                )
                .filter(warehouse -> !repository.existsByCode(warehouse.getCode()))
                .forEach(repository::save);


        System.out.println("✓ Warehouses seeded");

    }

}