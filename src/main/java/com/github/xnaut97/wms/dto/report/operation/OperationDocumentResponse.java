package com.github.xnaut97.wms.dto.report.operation;

import com.github.xnaut97.wms.enums.DocumentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class OperationDocumentResponse {

    private Long itemId;

    private Long documentId;

    private String documentNo;

    private LocalDate documentDate;

    private DocumentType documentType;

    public OperationDocumentResponse(
            Long itemId,
            Long documentId,
            String documentNo,
            LocalDate documentDate
    ) {

        this.itemId = itemId;

        this.documentId = documentId;

        this.documentNo = documentNo;

        this.documentDate = documentDate;

    }

}
