package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.enums.RoleType;
import com.github.xnaut97.wms.factory.SampleDataFactory;
import com.github.xnaut97.wms.repository.user.RoleRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final SampleDataFactory factory;

    @Transactional
    public void seed() {
        Stream.of(
                        factory.user(
                                "admin",
                                "System Administrator",
                                "admin@wms.com",
                                roleRepository.findByRole(RoleType.ADMIN).orElseThrow()
                        ),

                        factory.user(
                                "manager",
                                "Warehouse Manager",
                                "manager@wms.com",
                                roleRepository.findByRole(RoleType.WAREHOUSE_MANAGER).orElseThrow()
                        ),

                        factory.user(
                                "staff",
                                "Warehouse Staff",
                                "staff@wms.com",
                                roleRepository.findByRole(RoleType.WAREHOUSE_STAFF).orElseThrow()
                        ),

                        factory.user(
                                "board",
                                "Executive Board",
                                "board@wms.com",
                                roleRepository.findByRole(RoleType.EXECUTIVE_BOARD).orElseThrow()
                        ))
                .filter(user -> !userRepository.existsByEmail(user.getEmail())
                        || !userRepository.existsByUsername(user.getUsername()))
                .forEach(userRepository::save);

        System.out.println("✓ Users seeded");

    }

}