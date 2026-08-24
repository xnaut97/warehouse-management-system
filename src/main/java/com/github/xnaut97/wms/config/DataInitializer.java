package com.github.xnaut97.wms.config;

import com.github.xnaut97.wms.seed.UserSeeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserSeeder userSeeder;

    @Override
    public void run(String @NonNull ... args) {

        userSeeder.seed();
    }

}
