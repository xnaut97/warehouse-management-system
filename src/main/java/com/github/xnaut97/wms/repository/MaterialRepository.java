package com.github.xnaut97.wms.repository;

import com.github.xnaut97.wms.entity.material.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialRepository
        extends JpaRepository<Material, Long> {

    boolean existsByCode(String code);

    Optional<Material> findByCode(String code);

    Page<Material> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );

}