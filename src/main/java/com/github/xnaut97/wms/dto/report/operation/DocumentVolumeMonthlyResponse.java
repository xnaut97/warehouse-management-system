package com.github.xnaut97.wms.dto.report.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVolumeMonthlyResponse {

    private String month;

    private Long receiptCount;

    private Long issueCount;

    private Long totalCount;

}
