package com.github.xnaut97.wms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    private List<String> allowedOrigins = List.of();

    private List<String> allowedMethods = List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
    );

    private List<String> allowedHeaders = List.of("*");

    private List<String> exposedHeaders = List.of("Authorization");

    private boolean allowCredentials = true;

    private long maxAge = 3600;

}
