package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.enums.CustomerGroup;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CustomerSeeder {

    public static final String SCENARIO_CUSTOMER_CODE = "CUS006";

    private final CustomerRepository repository;
    private final SampleDataFactory factory;

    @Transactional
    public void seed() {

        Stream.of(

                        factory.customer("CUS001", "Samsung Vietnam", CustomerGroup.PROJECT),

                        factory.customer("CUS002", "LG Electronics", CustomerGroup.PROJECT),

                        factory.customer("CUS003", "Intel Products", CustomerGroup.AGENT),

                        factory.customer("CUS004", "Foxconn", CustomerGroup.AGENT),

                        factory.customer("CUS005", "Canon Vietnam", CustomerGroup.RETAIL),

                        factory.customer(
                                SCENARIO_CUSTOMER_CODE,
                                "Công ty Kiểm Thử Xuất Kho",
                                CustomerGroup.PROJECT
                        )

                )
                .filter(customer -> !repository.existsByCode(customer.getCode()))
                .forEach(repository::save);

        System.out.println("✓ Customers seeded");

    }

}
