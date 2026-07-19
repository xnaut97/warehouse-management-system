package com.github.xnaut97.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WMSApplication {

    public static void main(String[] args) {
        SpringApplication.run(WMSApplication.class, args);
    }

}
