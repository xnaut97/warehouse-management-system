package com.github.xnaut97.wms.repository.product;

import com.github.xnaut97.wms.entity.product.ProductReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductReceiptRepository extends JpaRepository<ProductReceipt, Long> {

    List<ProductReceipt> findTop10ByOrderByCreatedAtDesc();

}
