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

        return next(type, LocalDate.now());

    }

    @Transactional
    public String next(DocumentType type, LocalDate date) {

        DocumentSequence sequence =
                repository.findByDocumentTypeAndSequenceDate(type, date)
                        .orElseGet(() -> {

                            DocumentSequence s = new DocumentSequence();

                            s.setDocumentType(type);
                            s.setSequenceDate(date);
                            s.setCurrentValue(0);

                            return s;

                        });

        sequence.setCurrentValue(sequence.getCurrentValue() + 1);

        repository.save(sequence);

        return "%s%s-%04d".formatted(
                type.getPrefix(),
                date.format(FORMATTER),
                sequence.getCurrentValue()
        );

    }

}
