package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.enums.RoleType;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class WarehouseSeeder {

    private final WarehouseRepository repository;
    private final UserRepository userRepository;
    private final SampleDataFactory factory;

    @Transactional
    public void seed() {
        if (userRepository.count() == 0) return;
        if (repository.count() > 0) return;

        userRepository.findAll().stream()
                .filter(user -> user.getRole().getRole() == RoleType.WAREHOUSE_MANAGER)
                .findAny().ifPresent(user -> Stream.of(
                                factory.warehouse("WH001", "Main Warehouse", user),
                                factory.warehouse("WH002", "North Warehouse", user),
                                factory.warehouse("WH003", "South Warehouse", user)
                        )
                        .filter(warehouse -> !repository.existsByCode(warehouse.getCode()))
                        .forEach(repository::save));


        System.out.println("✓ Warehouses seeded");

    }

}