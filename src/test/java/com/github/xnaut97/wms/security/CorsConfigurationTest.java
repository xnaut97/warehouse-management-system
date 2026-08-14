package com.github.xnaut97.wms.security;

import com.github.xnaut97.wms.config.CorsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigurationTest {

    private static final String PRODUCTION_ORIGIN =
            "https://warehouse-management-rosy.vercel.app";

    private static final String LOCAL_ORIGIN = "http://localhost:5173";

    private CorsConfiguration configurationFor(List<String> origins) {

        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(origins);

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/stocktaking");

        return new SecurityConfig(null, properties)
                .corsConfigurationSource()
                .getCorsConfiguration(request);
    }

    @Test
    void allowsProductionAndLocalOrigins() {

        CorsConfiguration configuration =
                configurationFor(List.of(PRODUCTION_ORIGIN, LOCAL_ORIGIN));

        assertEquals(
                PRODUCTION_ORIGIN,
                configuration.checkOrigin(PRODUCTION_ORIGIN)
        );

        assertEquals(
                LOCAL_ORIGIN,
                configuration.checkOrigin(LOCAL_ORIGIN)
        );
    }

    @Test
    void rejectsUnknownOrigin() {

        CorsConfiguration configuration =
                configurationFor(List.of(PRODUCTION_ORIGIN));

        assertNull(configuration.checkOrigin("https://evil.example.com"));
    }

    @Test
    void allowsEveryHttpMethodUsedByTheApi() {

        CorsConfiguration configuration =
                configurationFor(List.of(PRODUCTION_ORIGIN));

        assertTrue(
                configuration.getAllowedMethods()
                        .containsAll(
                                List.of(
                                        "GET",
                                        "POST",
                                        "PUT",
                                        "PATCH",
                                        "DELETE",
                                        "OPTIONS"
                                )
                        )
        );

        assertEquals(
                Boolean.TRUE,
                configuration.getAllowCredentials()
        );
    }

    @Test
    void rejectsWildcardAndEmptyOriginConfiguration() {

        assertThrows(
                IllegalStateException.class,
                () -> configurationFor(List.of("*"))
        );

        assertThrows(
                IllegalStateException.class,
                () -> configurationFor(List.of())
        );
    }

}
