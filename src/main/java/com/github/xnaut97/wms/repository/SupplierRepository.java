package com.github.xnaut97.wms.repository;

import com.github.xnaut97.wms.entity.material.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByCode(String code);

    Optional<Supplier> findByCode(String code);

    List<Supplier> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
            String code,
            String name
    );
}