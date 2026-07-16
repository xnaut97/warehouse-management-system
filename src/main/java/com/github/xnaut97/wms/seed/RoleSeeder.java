package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.enums.RoleType;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleRepository repository;

    private final SampleDataFactory factory;

    @Transactional
    public void seed() {
        if (repository.count() > 0) {
            return;
        }

        for (RoleType role : RoleType.values()) {
            if(repository.existsByRole(role))
                continue;

            repository.save(factory.role(role));
        }

        System.out.println("✓ Roles seeded");

    }

}