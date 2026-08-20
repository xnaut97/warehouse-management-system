package com.github.xnaut97.wms.service.product;

import com.github.xnaut97.wms.dto.product.issue.AddProductIssueItemRequest;
import com.github.xnaut97.wms.dto.product.issue.ProductIssueItemResponse;
import com.github.xnaut97.wms.dto.product.issue.UpdateProductIssueItemRequest;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.entity.product.ProductIssue;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.product.ProductIssueRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The issue document must never be able to hold more than the lot actually
 * has, no matter what the client sends, and the expiration date always comes
 * from the lot rather than from the request.
 */
@SpringBootTest
@Transactional
class ProductIssueStockValidationTest {

    private static final String LOT_A = "LOT-A";

    private static final String LOT_B = "LOT-B";

    private static final LocalDate EXPIRY_A = LocalDate.of(2027, 1, 31);

    private static final LocalDate EXPIRY_B = LocalDate.of(2026, 12, 31);

    @Autowired
    private ProductIssueService service;

    @Autowired
    private ProductIssueRepository issueRepository;

    @Autowired
    private ProductInventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Product product;

    private ProductIssue issue;

    @BeforeEach
    void setUp() {

        List<User> users = userRepository.findAll();

        assumeTrue(
                !users.isEmpty(),
                "needs at least one seeded user to own the issue document"
        );

        product = issuableProduct();

        assumeTrue(
                product != null,
                "needs a product the product_issue_items foreign key accepts"
        );

        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        Warehouse warehouse = new Warehouse();
        warehouse.setCode("TEST-WH-" + suffix);
        warehouse.setName("Kho kiểm thử " + suffix);
        warehouseRepository.save(warehouse);

        saveInventory(warehouse, LOT_A, "10", EXPIRY_A);
        saveInventory(warehouse, LOT_B, "5", EXPIRY_B);

        issue = new ProductIssue();
        issue.setIssueNo("TEST-PXK-" + suffix);
        issue.setWarehouse(warehouse);
        issue.setIssueDate(LocalDate.now());
        issue.setStatus(IssueStatus.DRAFT);
        issue.setCreatedBy(users.get(0));
        issue.setTotalAmount(BigDecimal.ZERO);
        issueRepository.save(issue);
    }

    @Test
    void addsItemWhenQuantityFitsInTheSelectedLot() {

        ProductIssueItemResponse response =
                service.addItem(
                        issue.getId(),
                        addRequest(LOT_A, "10", "1500")
                );

        assertThat(response.getQuantity())
                .isEqualByComparingTo("10");

        assertThat(response.getLotNumber())
                .isEqualTo(LOT_A);

        assertThat(response.getAmount())
                .isEqualByComparingTo("15000");
    }

    @Test
    void takesExpirationDateFromTheLotAndNotFromTheRequest() {

        AddProductIssueItemRequest request =
                addRequest(LOT_B, "1", null);

        request.setExpirationDate(LocalDate.of(2099, 1, 1));

        ProductIssueItemResponse response =
                service.addItem(issue.getId(), request);

        assertThat(response.getExpirationDate())
                .isEqualTo(EXPIRY_B);
    }

    @Test
    void rejectsQuantityAboveTheStockOfTheSelectedLot() {

        assertThatThrownBy(() ->
                service.addItem(
                        issue.getId(),
                        addRequest(LOT_B, "6", null)
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không đủ")
                .hasMessageContaining("5");
    }

    @Test
    void rejectsALotThatDoesNotExistInTheWarehouse() {

        assertThatThrownBy(() ->
                service.addItem(
                        issue.getId(),
                        addRequest("LOT-UNKNOWN", "1", null)
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không tìm thấy tồn kho");
    }

    @Test
    void rejectsAnUpdateThatPushesTheLineAboveTheLotStock() {

        ProductIssueItemResponse item =
                service.addItem(
                        issue.getId(),
                        addRequest(LOT_A, "4", null)
                );

        UpdateProductIssueItemRequest request =
                new UpdateProductIssueItemRequest();

        request.setQuantity(new BigDecimal("11"));
        request.setLotNumber(LOT_A);

        assertThatThrownBy(() ->
                service.updateItem(
                        issue.getId(),
                        item.getId(),
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không đủ");
    }

    @Test
    void movingTheLineToAnotherLotUpdatesTheExpirationDate() {

        ProductIssueItemResponse item =
                service.addItem(
                        issue.getId(),
                        addRequest(LOT_A, "2", null)
                );

        assertThat(item.getExpirationDate())
                .isEqualTo(EXPIRY_A);

        UpdateProductIssueItemRequest request =
                new UpdateProductIssueItemRequest();

        request.setQuantity(new BigDecimal("2"));
        request.setLotNumber(LOT_B);

        ProductIssueItemResponse updated =
                service.updateItem(
                        issue.getId(),
                        item.getId(),
                        request
                );

        assertThat(updated.getLotNumber())
                .isEqualTo(LOT_B);

        assertThat(updated.getExpirationDate())
                .isEqualTo(EXPIRY_B);
    }

    @Test
    void confirmDeductsOnlyFromTheSelectedLot() {

        service.addItem(
                issue.getId(),
                addRequest(LOT_B, "3", "2000")
        );

        service.confirm(issue.getId());

        assertThat(lotQuantity(LOT_B))
                .isEqualByComparingTo("2");

        assertThat(lotQuantity(LOT_A))
                .isEqualByComparingTo("10");

        assertThat(issueRepository.findById(issue.getId()).orElseThrow().getStatus())
                .isEqualTo(IssueStatus.CONFIRMED);
    }

    /**
     * The products table was renamed from finished_products while the child
     * foreign keys were left pointing at the old table, so only products that
     * still exist in both can be put on an issue document. The fixture works
     * with a warehouse of its own, so it never touches real stock rows.
     */
    private Product issuableProduct() {

        List<?> ids = entityManager.createNativeQuery("""
                        SELECT p.id
                        FROM products p
                        JOIN finished_products f ON f.id = p.id
                        WHERE p.enabled = TRUE
                        """)
                .setMaxResults(1)
                .getResultList();

        if (ids.isEmpty()) {
            return null;
        }

        return productRepository
                .findById(((Number) ids.get(0)).longValue())
                .orElse(null);
    }

    private AddProductIssueItemRequest addRequest(
            String lotNumber,
            String quantity,
            String unitPrice
    ) {

        AddProductIssueItemRequest request =
                new AddProductIssueItemRequest();

        request.setProductId(product.getId());
        request.setQuantity(new BigDecimal(quantity));
        request.setLotNumber(lotNumber);

        if (unitPrice != null) {
            request.setUnitPrice(new BigDecimal(unitPrice));
        }

        return request;
    }

    private void saveInventory(
            Warehouse warehouse,
            String lotNumber,
            String quantity,
            LocalDate expirationDate
    ) {

        ProductInventory inventory = new ProductInventory();

        inventory.setWarehouse(warehouse);
        inventory.setProduct(product);
        inventory.setLotNumber(lotNumber);
        inventory.setQuantity(new BigDecimal(quantity));
        inventory.setExpirationDate(expirationDate);

        inventoryRepository.save(inventory);
    }

    private BigDecimal lotQuantity(String lotNumber) {

        return inventoryRepository
                .findByWarehouseProductAndLot(
                        issue.getWarehouse().getId(),
                        product.getId(),
                        lotNumber
                )
                .orElseThrow()
                .getQuantity();
    }
}
