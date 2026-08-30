package com.github.xnaut97.wms.repository.product;

import com.github.xnaut97.wms.entity.product.ProductReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductReceiptItemRepository extends JpaRepository<ProductReceiptItem, Long> {

    List<ProductReceiptItem> findByReceiptId(Long receiptId);

    boolean existsByReceiptIdAndProductId(Long receiptId, Long productId);

    Optional<ProductReceiptItem> findByIdAndReceiptId(Long id, Long receiptId);

    @Query("""
            SELECT COALESCE(SUM(i.quantity),0)
            FROM ProductReceiptItem i
            """)
    BigDecimal getTotalQuantity();

}
