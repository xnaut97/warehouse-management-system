package com.github.xnaut97.wms.repository.product;

import com.github.xnaut97.wms.entity.product.ProductIssueItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductIssueItemRepository extends JpaRepository<ProductIssueItem, Long> {

    List<ProductIssueItem> findByIssueId(Long issueId);

    boolean existsByIssueIdAndProductId(Long issueId, Long productId);

}
