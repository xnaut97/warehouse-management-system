package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class NearExpirationAlertResponse {

    private String code;

    private String name;

    private LocalDate expirationDate;

    private long daysLeft;

}
