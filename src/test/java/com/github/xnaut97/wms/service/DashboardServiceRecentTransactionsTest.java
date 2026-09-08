package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.dto.dashboard.RecentTransactionResponse;
import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.entity.goods.GoodsIssueItem;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.entity.product.ProductIssue;
import com.github.xnaut97.wms.entity.product.ProductIssueItem;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.repository.CustomerRepository;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.SupplierRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.product.ProductIssueItemRepository;
import com.github.xnaut97.wms.repository.product.ProductIssueRepository;
import com.github.xnaut97.wms.repository.product.ProductReceiptRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingItemRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceRecentTransactionsTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private MaterialInventoryRepository materialInventoryRepository;
    @Mock
    private ProductInventoryRepository productInventoryRepository;
    @Mock
    private GoodsReceiptRepository receiptRepository;
    @Mock
    private GoodsIssueRepository issueRepository;
    @Mock
    private GoodsReceiptItemRepository receiptItemRepository;
    @Mock
    private GoodsIssueItemRepository issueItemRepository;
    @Mock
    private ProductReceiptRepository productReceiptRepository;
    @Mock
    private ProductIssueRepository productIssueRepository;
    @Mock
    private ProductIssueItemRepository productIssueItemRepository;
    @Mock
    private InventoryTransactionRepository transactionRepository;
    @Mock
    private StocktakingRepository stocktakingRepository;
    @Mock
    private StocktakingItemRepository stocktakingItemRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    @Test
    void recentTransactions_IncludesRealtimeGoodsAndProductIssues() {
        // Arrange GoodsIssue (Material)
        GoodsIssue goodsIssue = new GoodsIssue();
        goodsIssue.setId(101L);
        goodsIssue.setIssueNo("GI-2026-001");
        goodsIssue.setStatus(IssueStatus.PENDING);
        goodsIssue.setCreatedAt(LocalDateTime.of(2026, 9, 9, 10, 30));

        Material mat = new Material();
        mat.setCode("MAT-001");

        GoodsIssueItem giItem = new GoodsIssueItem();
        giItem.setMaterial(mat);
        giItem.setQuantity(new BigDecimal("50"));

        when(issueRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(goodsIssue));
        when(issueItemRepository.findByIssueId(101L)).thenReturn(List.of(giItem));

        // Arrange ProductIssue (Product)
        ProductIssue productIssue = new ProductIssue();
        productIssue.setId(201L);
        productIssue.setIssueNo("PI-2026-001");
        productIssue.setStatus(IssueStatus.CONFIRMED);
        productIssue.setCreatedAt(LocalDateTime.of(2026, 9, 9, 11, 00)); // newer

        Product prod = new Product();
        prod.setCode("PROD-001");

        ProductIssueItem piItem = new ProductIssueItem();
        piItem.setProduct(prod);
        piItem.setQuantity(new BigDecimal("20"));
        productIssue.setItems(List.of(piItem));

        when(productIssueRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(productIssue));

        when(receiptRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        when(productReceiptRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        when(transactionRepository.findRecentTransactions(any(Pageable.class))).thenReturn(Collections.emptyList());

        // Act
        List<RecentTransactionResponse> results = dashboardService.recentTransactions();

        // Assert
        assertThat(results).hasSize(2);

        // Newer product issue should come first
        RecentTransactionResponse first = results.get(0);
        assertThat(first.getVoucherNo()).isEqualTo("PI-2026-001");
        assertThat(first.getItemCategory()).isEqualTo("Thành phẩm");
        assertThat(first.getItemCode()).isEqualTo("PROD-001");
        assertThat(first.getTransactionType()).isEqualTo("ISSUE");
        assertThat(first.getStatus()).isEqualTo("CONFIRMED");
        assertThat(first.getTime()).isEqualTo("09/09/2026 11:00");

        // Older goods issue
        RecentTransactionResponse second = results.get(1);
        assertThat(second.getVoucherNo()).isEqualTo("GI-2026-001");
        assertThat(second.getItemCategory()).isEqualTo("Nguyên vật liệu");
        assertThat(second.getItemCode()).isEqualTo("MAT-001");
        assertThat(second.getTransactionType()).isEqualTo("ISSUE");
        assertThat(second.getStatus()).isEqualTo("PENDING");
        assertThat(second.getTime()).isEqualTo("09/09/2026 10:30");
    }
}
