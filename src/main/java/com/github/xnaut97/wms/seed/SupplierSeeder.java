package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.enums.SupplierGroup;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SupplierSeeder {

    public static final String SAND_CODE = "SUP006";

    public static final String CEMENT_CODE = "SUP007";

    public static final String ADDITIVE_CODE = "SUP008";

    public static final String PACKAGING_CODE = "SUP009";

    private final SupplierRepository repository;

    private final SampleDataFactory factory;

    @Transactional
    public void seed() {

        Stream.of(
                        factory.supplier(
                                "SUP001",
                                "Global Steel",
                                SupplierGroup.SAND
                        ),

                        factory.supplier(
                                "SUP002",
                                "ABC Metals",
                                SupplierGroup.CEMENT
                        ),

                        factory.supplier(
                                "SUP003",
                                "Viet Plastic",
                                SupplierGroup.PACKAGING_MATERIAL
                        ),

                        factory.supplier(
                                "SUP004",
                                "Industrial Components",
                                SupplierGroup.ADDITIVE
                        ),

                        factory.supplier(
                                "SUP005",
                                "Asia Materials",
                                SupplierGroup.SAND
                        ),

                        factory.supplier(
                                SAND_CODE,
                                "Công ty TNHH Cát Nghiền Đồng Nai",
                                SupplierGroup.SAND
                        ),

                        factory.supplier(
                                CEMENT_CODE,
                                "Công ty CP Xi Măng Bình An",
                                SupplierGroup.CEMENT
                        ),

                        factory.supplier(
                                ADDITIVE_CODE,
                                "Công ty TNHH Phụ Gia Xây Dựng An Phát",
                                SupplierGroup.ADDITIVE
                        ),

                        factory.supplier(
                                PACKAGING_CODE,
                                "Công ty TNHH Bao Bì Tân Thành",
                                SupplierGroup.PACKAGING_MATERIAL
                        ))
                .filter(supplier -> !repository.existsByCode(supplier.getCode()))
                .forEach(repository::save);

        System.out.println("✓ Suppliers seeded");

    }

}
