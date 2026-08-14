package com.github.xnaut97.wms.security;

import com.github.xnaut97.wms.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CorsPreflightTest.ProbeController.class)
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        JwtAuthenticationFilter.class,
        CorsPreflightTest.ProbeController.class
})
@TestPropertySource(properties =
        "cors.allowed-origins=https://warehouse-management-rosy.vercel.app,http://localhost:5173")
class CorsPreflightTest {

    private static final String PRODUCTION_ORIGIN =
            "https://warehouse-management-rosy.vercel.app";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void preflightIsAnsweredWithoutAuthentication() throws Exception {

        mockMvc.perform(
                        options("/api/probe")
                                .header("Origin", PRODUCTION_ORIGIN)
                                .header("Access-Control-Request-Method", "GET")
                                .header("Access-Control-Request-Headers", "authorization")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        PRODUCTION_ORIGIN
                ))
                .andExpect(header().string(
                        "Access-Control-Allow-Credentials",
                        "true"
                ));
    }

    @Test
    void preflightForWriteMethodIsAllowed() throws Exception {

        mockMvc.perform(
                        options("/api/probe")
                                .header("Origin", PRODUCTION_ORIGIN)
                                .header("Access-Control-Request-Method", "POST")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        PRODUCTION_ORIGIN
                ));
    }

    @Test
    void preflightFromUnknownOriginIsRejected() throws Exception {

        mockMvc.perform(
                        options("/api/probe")
                                .header("Origin", "https://evil.example.com")
                                .header("Access-Control-Request-Method", "GET")
                )
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void unauthenticatedRequestStillFailsWithCorsHeaders() throws Exception {

        mockMvc.perform(
                        get("/api/probe")
                                .header("Origin", PRODUCTION_ORIGIN)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        PRODUCTION_ORIGIN
                ));
    }

    @RestController
    static class ProbeController {

        @GetMapping("/api/probe")
        String read() {
            return "ok";
        }

        @PostMapping("/api/probe")
        String write() {
            return "ok";
        }

    }

}
