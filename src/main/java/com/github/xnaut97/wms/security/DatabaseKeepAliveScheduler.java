package com.github.xnaut97.wms.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseKeepAliveScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(fixedDelay = 5 * 60 * 1000) // Every 5 minutes
    public void keepAlive() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.debug("Database keep-alive successful.");
        } catch (Exception e) {
            log.warn("Database keep-alive failed.", e);
        }
    }

}