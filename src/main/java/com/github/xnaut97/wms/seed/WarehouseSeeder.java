package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.enums.RoleType;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WarehouseSeeder {

    private final WarehouseRepository repository;
    private final UserRepository userRepository;
    private final SampleDataFactory factory;

    @Transactional
    public void seed() {
        if (userRepository.count() == 0) return;

        userRepository.findAll().stream()
                .filter(user -> user.getRole().getRole() == RoleType.WAREHOUSE_MANAGER)
                .findAny().ifPresent(user -> {
                    upsertStorageArea(
                            WarehouseService.MATERIAL_WAREHOUSE_CODE,
                            "Kho nguyên vật liệu",
                            user
                    );
                    upsertStorageArea(
                            WarehouseService.PRODUCT_WAREHOUSE_CODE,
                            "Kho sản phẩm",
                            user
                    );
                });


        System.out.println("✓ Warehouses seeded");

    }

    private void upsertStorageArea(
            String code,
            String name,
            com.github.xnaut97.wms.entity.user.User manager
    ) {

        repository.findByCode(code)
                .ifPresentOrElse(warehouse -> {
                    warehouse.setName(name);
                    warehouse.setDescription(null);
                    warehouse.setManager(manager);
                    warehouse.setEnabled(true);
                    repository.save(warehouse);
                }, () -> repository.save(factory.warehouse(code, name, manager)));

    }

}
