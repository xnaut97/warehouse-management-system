package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.enums.CustomerGroup;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CustomerSeeder {

    public static final List<String> CODES = List.of(
            "CUS001",
            "CUS002",
            "CUS003",
            "CUS004",
            "CUS005",
            "CUS007",
            "CUS008",
            "CUS009",
            "CUS010",
            "CUS011",
            "CUS012"
    );

    private final CustomerRepository repository;

    private final SampleDataFactory factory;

    @Transactional
    public void seed() {

        Stream.of(
                        factory.customer(
                                "CUS001",
                                "Samsung Vietnam",
                                CustomerGroup.PROJECT
                        ),

                        factory.customer(
                                "CUS002",
                                "LG Electronics",
                                CustomerGroup.PROJECT
                        ),

                        factory.customer(
                                "CUS003",
                                "Intel Products",
                                CustomerGroup.AGENT
                        ),

                        factory.customer(
                                "CUS004",
                                "Foxconn",
                                CustomerGroup.AGENT
                        ),

                        factory.customer(
                                "CUS005",
                                "Canon Vietnam",
                                CustomerGroup.RETAIL
                        ),

                        factory.customer(
                                "CUS007",
                                "Công ty CP Xây Dựng Nam Long",
                                CustomerGroup.PROJECT
                        ),

                        factory.customer(
                                "CUS008",
                                "Công ty TNHH Xây Dựng Đại Phúc",
                                CustomerGroup.PROJECT
                        ),

                        factory.customer(
                                "CUS009",
                                "Đại lý VLXD Hoàng Gia",
                                CustomerGroup.AGENT
                        ),

                        factory.customer(
                                "CUS010",
                                "Đại lý Gạch Men Thành Đạt",
                                CustomerGroup.AGENT
                        ),

                        factory.customer(
                                "CUS011",
                                "Cửa hàng VLXD Minh Tâm",
                                CustomerGroup.RETAIL
                        ),

                        factory.customer(
                                "CUS012",
                                "Cửa hàng Trang Trí Nội Thất An Cư",
                                CustomerGroup.RETAIL
                        ))
                .filter(customer -> !repository.existsByCode(customer.getCode()))
                .forEach(repository::save);

        System.out.println("✓ Customers seeded");

    }

}
