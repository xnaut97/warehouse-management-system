package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class WarehouseSeeder {

    private final WarehouseRepository warehouseRepository;

    private final UserRepository userRepository;

    private final SampleDataFactory factory;

    @Transactional
    public void seed() {

        User manager = userRepository.findByUsername("manager").orElseThrow();

        Stream.of(
                        factory.warehouse(
                                WarehouseService.MATERIAL_WAREHOUSE_CODE,
                                "Kho nguyên vật liệu",
                                "Lô C3, Khu công nghiệp Tân Bình, Quận Tân Phú, TP. Hồ Chí Minh",
                                "Kho lưu trữ nguyên vật liệu đầu vào phục vụ sản xuất",
                                manager
                        ),

                        factory.warehouse(
                                WarehouseService.PRODUCT_WAREHOUSE_CODE,
                                "Kho sản phẩm",
                                "Lô A7, Khu công nghiệp Sóng Thần 1, TP. Dĩ An, Bình Dương",
                                "Kho lưu trữ thành phẩm chờ xuất bán cho khách hàng",
                                manager
                        ))
                .filter(warehouse -> !warehouseRepository.existsByCode(warehouse.getCode()))
                .forEach(warehouseRepository::save);

        System.out.println("✓ Warehouses seeded");

    }

}
