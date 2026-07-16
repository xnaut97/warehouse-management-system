package com.github.xnaut97.wms.repository;

import com.github.xnaut97.wms.entity.material.RawMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RawMaterialRepository
        extends JpaRepository<RawMaterial, Long> {

    boolean existsByCode(String code);

    Optional<RawMaterial> findByCode(String code);

    Page<RawMaterial> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );

}