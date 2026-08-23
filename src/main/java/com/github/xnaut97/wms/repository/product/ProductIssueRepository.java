package com.github.xnaut97.wms.repository.product;

import com.github.xnaut97.wms.entity.product.ProductIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductIssueRepository extends JpaRepository<ProductIssue, Long> {

    boolean existsByCustomerCode(String code);

}
