package com.github.xnaut97.wms.repository;

import com.github.xnaut97.wms.entity.FinishedProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinishedProductRepository
        extends JpaRepository<FinishedProduct, Long> {

    boolean existsByCode(String code);

    Page<FinishedProduct>
    findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(

            String code,

            String name,

            Pageable pageable

    );

}