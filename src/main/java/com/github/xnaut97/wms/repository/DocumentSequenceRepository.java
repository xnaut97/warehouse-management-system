package com.github.xnaut97.wms.repository;

import com.github.xnaut97.wms.entity.common.DocumentSequence;
import com.github.xnaut97.wms.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DocumentSequenceRepository
        extends JpaRepository<DocumentSequence, Long> {

    Optional<DocumentSequence> findByDocumentTypeAndSequenceDate(
            DocumentType documentType,
            LocalDate sequenceDate
    );

}