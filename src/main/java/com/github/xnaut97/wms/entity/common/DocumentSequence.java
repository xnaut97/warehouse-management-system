package com.github.xnaut97.wms.entity.common;

import com.github.xnaut97.wms.entity.BaseEntity;
import com.github.xnaut97.wms.enums.DocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "document_sequences",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "documentType",
                        "sequenceDate"
                })
        }
)
public class DocumentSequence extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private LocalDate sequenceDate;

    @Column(nullable = false)
    private Integer currentValue;

}