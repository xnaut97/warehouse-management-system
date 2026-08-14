package com.github.xnaut97.wms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * Origins allowed to call the API. Exact origins or patterns
     * (e.g. https://*.vercel.app). "*" is rejected because the API
     * is served with credentials enabled.
     */
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

    /**
     * How long (seconds) the browser may cache a preflight response.
     */
    private long maxAge = 3600;

}
