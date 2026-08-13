package com.github.xnaut97.wms.repository.bom;

import com.github.xnaut97.wms.entity.bom.BOM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BOMRepository extends JpaRepository<BOM, Long> {

    boolean existsByCode(String code);

    List<BOM> findByCodeContainingIgnoreCase(String keyword);

}