package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.entity.common.DocumentSequence;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.repository.DocumentSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DocumentNumberService {

    private final DocumentSequenceRepository repository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.BASIC_ISO_DATE;

    @Transactional
    public String next(DocumentType type) {

        LocalDate today = LocalDate.now();

        DocumentSequence sequence =
                repository.findByDocumentTypeAndSequenceDate(type, today)
                        .orElseGet(() -> {

                            DocumentSequence s = new DocumentSequence();

                            s.setDocumentType(type);
                            s.setSequenceDate(today);
                            s.setCurrentValue(0);

                            return s;

                        });

        sequence.setCurrentValue(sequence.getCurrentValue() + 1);

        repository.save(sequence);

        return "%s%s-%04d".formatted(
                type.getPrefix(),
                today.format(FORMATTER),
                sequence.getCurrentValue()
        );

    }

}