package com.github.xnaut97.wms.repository;

import com.github.xnaut97.wms.entity.common.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface WarehouseRepository
        extends JpaRepository<Warehouse, Long> {

    boolean existsByCode(String code);

    Optional<Warehouse> findByCode(String code);

    Page<Warehouse> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );

    Page<Warehouse> findByCodeIn(Collection<String> codes, Pageable pageable);

}
