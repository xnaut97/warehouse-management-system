package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.dto.issue.AddIssueItemRequest;
import com.github.xnaut97.wms.dto.issue.IssueItemResponse;
import com.github.xnaut97.wms.dto.issue.UpdateIssueItemRequest;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
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
 * A raw material issue line may only take what the warehouse of the document
 * actually holds, minus what the rest of the document already takes from the
 * same material.
 */
@SpringBootTest
@Transactional
class IssueStockValidationTest {

    @Autowired
    private IssueService service;

    @Autowired
    private GoodsIssueRepository issueRepository;

    @Autowired
    private MaterialInventoryRepository inventoryRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    private Material material;

    private Material materialWithoutStock;

    private GoodsIssue issue;

    @BeforeEach
    void setUp() {

        List<User> users = userRepository.findAll();

        assumeTrue(
                !users.isEmpty(),
                "needs at least one seeded user to own the issue document"
        );

        List<Material> materials = materialRepository.findAll();

        assumeTrue(
                materials.size() >= 2,
                "needs two seeded materials to separate the stocked one"
        );

        material = materials.get(0);
        materialWithoutStock = materials.get(1);

        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        Warehouse warehouse = new Warehouse();
        warehouse.setCode("TEST-WH-" + suffix);
        warehouse.setName("Kho kiểm thử " + suffix);
        warehouseRepository.save(warehouse);

        MaterialInventory inventory = new MaterialInventory();
        inventory.setWarehouse(warehouse);
        inventory.setMaterial(material);
        inventory.setQuantity(new BigDecimal("10"));
        inventoryRepository.save(inventory);

        issue = new GoodsIssue();
        issue.setIssueNo("TEST-PXNVL-" + suffix);
        issue.setWarehouse(warehouse);
        issue.setIssueDate(LocalDate.now());
        issue.setStatus(IssueStatus.DRAFT);
        issue.setCreatedBy(users.get(0));
        issue.setTotalAmount(BigDecimal.ZERO);
        issueRepository.save(issue);
    }

    @Test
    void addsItemWhenTheWarehouseHasEnoughStock() {

        IssueItemResponse response =
                service.addItem(
                        issue.getId(),
                        addRequest(material, "10")
                );

        assertThat(response.getMaterialId())
                .isEqualTo(material.getId());

        assertThat(response.getQuantity())
                .isEqualByComparingTo("10");
    }

    @Test
    void rejectsQuantityAboveTheWarehouseStock() {

        assertThatThrownBy(() ->
                service.addItem(
                        issue.getId(),
                        addRequest(material, "11")
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không đủ")
                .hasMessageContaining("10");
    }

    @Test
    void rejectsAMaterialThatHasNoInventoryInTheWarehouse() {

        assertThatThrownBy(() ->
                service.addItem(
                        issue.getId(),
                        addRequest(materialWithoutStock, "1")
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không tìm thấy tồn kho");
    }

    @Test
    void rejectsAMaterialThatDoesNotExist() {

        AddIssueItemRequest request = new AddIssueItemRequest();
        request.setMaterialId(Long.MAX_VALUE);
        request.setQuantity(BigDecimal.ONE);

        assertThatThrownBy(() ->
                service.addItem(issue.getId(), request)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không tìm thấy nguyên liệu");
    }

    @Test
    void allowsASecondLineWhileStockIsLeft() {

        service.addItem(
                issue.getId(),
                addRequest(material, "3")
        );

        IssueItemResponse second =
                service.addItem(
                        issue.getId(),
                        addRequest(material, "7")
                );

        assertThat(second.getQuantity())
                .isEqualByComparingTo("7");
    }

    @Test
    void rejectsASecondLineThatExceedsWhatIsLeft() {

        service.addItem(
                issue.getId(),
                addRequest(material, "3")
        );

        assertThatThrownBy(() ->
                service.addItem(
                        issue.getId(),
                        addRequest(material, "8")
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không đủ")
                .hasMessageContaining("7");
    }

    @Test
    void anUpdateDoesNotCountTheLineItIsEditingTwice() {

        IssueItemResponse item =
                service.addItem(
                        issue.getId(),
                        addRequest(material, "4")
                );

        UpdateIssueItemRequest request = new UpdateIssueItemRequest();
        request.setQuantity(new BigDecimal("10"));

        IssueItemResponse updated =
                service.updateItem(
                        issue.getId(),
                        item.getId(),
                        request
                );

        assertThat(updated.getQuantity())
                .isEqualByComparingTo("10");
    }

    @Test
    void rejectsAnUpdateThatPushesTheDocumentAboveTheStock() {

        service.addItem(
                issue.getId(),
                addRequest(material, "6")
        );

        IssueItemResponse second =
                service.addItem(
                        issue.getId(),
                        addRequest(material, "2")
                );

        UpdateIssueItemRequest request = new UpdateIssueItemRequest();
        request.setQuantity(new BigDecimal("5"));

        assertThatThrownBy(() ->
                service.updateItem(
                        issue.getId(),
                        second.getId(),
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không đủ");
    }

    @Test
    void confirmDeductsEveryLineFromTheWarehouseStock() {

        service.addItem(
                issue.getId(),
                addRequest(material, "3")
        );

        service.addItem(
                issue.getId(),
                addRequest(material, "2")
        );

        service.confirm(issue.getId());

        assertThat(stockQuantity())
                .isEqualByComparingTo("5");

        assertThat(
                issueRepository.findById(issue.getId())
                        .orElseThrow()
                        .getStatus()
        )
                .isEqualTo(IssueStatus.CONFIRMED);
    }

    private AddIssueItemRequest addRequest(
            Material target,
            String quantity
    ) {

        AddIssueItemRequest request = new AddIssueItemRequest();

        request.setMaterialId(target.getId());
        request.setQuantity(new BigDecimal(quantity));

        return request;
    }

    private BigDecimal stockQuantity() {

        return inventoryRepository
                .findByWarehouseIdAndMaterialId(
                        issue.getWarehouse().getId(),
                        material.getId()
                )
                .orElseThrow()
                .getQuantity();
    }
}
