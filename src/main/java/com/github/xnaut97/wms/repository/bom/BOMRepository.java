package com.github.xnaut97.wms.repository.bom;

import com.github.xnaut97.wms.entity.bom.BOM;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BOMRepository extends JpaRepository<BOM, Long> {

    boolean existsByCode(String code);

    List<BOM> findByCodeContainingIgnoreCase(String keyword);

    @EntityGraph(attributePaths = {"items", "items.material"})
    Optional<BOM> findFirstByProductIdAndEnabledTrue(Long productId);

}