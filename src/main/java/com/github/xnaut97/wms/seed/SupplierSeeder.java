package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SupplierSeeder {

    private final SupplierRepository repository;
    private final SampleDataFactory factory;

    @Transactional
    public void seed() {
        Stream.of(
                        factory.supplier("SUP001", "Global Steel"),
                        factory.supplier("SUP002", "ABC Metals"),
                        factory.supplier("SUP003", "Viet Plastic"),
                        factory.supplier("SUP004", "Industrial Components"),
                        factory.supplier("SUP005", "Asia Materials")

                )
                .filter(s -> !repository.existsByCode(s.getCode()))
                .forEach(repository::save);


        System.out.println("✓ Suppliers seeded");

    }

}