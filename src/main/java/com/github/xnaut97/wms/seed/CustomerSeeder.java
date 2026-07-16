package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerSeeder {

    private final CustomerRepository repository;
    private final SampleDataFactory factory;

    @Transactional
    public void seed() {

        if(repository.count() > 0){
            return;
        }

        repository.saveAll(

                List.of(

                        factory.customer("CUS001","Samsung Vietnam"),

                        factory.customer("CUS002","LG Electronics"),

                        factory.customer("CUS003","Intel Products"),

                        factory.customer("CUS004","Foxconn"),

                        factory.customer("CUS005","Canon Vietnam")

                )

        );

        System.out.println("✓ Customers seeded");

    }

}