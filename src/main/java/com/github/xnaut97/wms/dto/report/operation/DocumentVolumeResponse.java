package com.github.xnaut97.wms.dto.report.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVolumeResponse {

    private Long materialReceipts;

    private Long productReceipts;

    private Long materialIssues;

    private Long productIssues;

    private Long totalReceipts;

    private Long totalIssues;

    private Long totalDocuments;

    private List<DocumentVolumeMonthlyResponse> monthly;

}
