package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.issue.AddIssueItemRequest;
import com.github.xnaut97.wms.dto.issue.IssueRequest;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.repository.CustomerRepository;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
import com.github.xnaut97.wms.service.IssueService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueScenarioSeeder {

    private record IssueLine(
            String materialCode,
            String quantity
    ) {
    }

    private static final List<IssueLine> CONFIRMED_LINES = List.of(
            new IssueLine(MaterialSeeder.MATERIAL_C2, "25"),
            new IssueLine(MaterialSeeder.MATERIAL_RESIN, "10")
    );

    private static final List<IssueLine> DRAFT_LINES = List.of(
            new IssueLine(MaterialSeeder.MATERIAL_C1, "100"),
            new IssueLine(MaterialSeeder.MATERIAL_C1, "50"),
            new IssueLine(MaterialSeeder.MATERIAL_RESIN, "10")
    );

    private final IssueService issueService;

    private final GoodsIssueRepository issueRepository;

    private final CustomerRepository customerRepository;

    private final WarehouseRepository warehouseRepository;

    private final MaterialRepository materialRepository;

    @Transactional
    public void seed() {

        if (issueRepository.existsByCustomerCode(
                CustomerSeeder.SCENARIO_CUSTOMER_CODE
        )) {
            return;
        }

        Optional<Warehouse> warehouse =
                warehouseRepository.findByCode(
                        WarehouseService.MATERIAL_WAREHOUSE_CODE
                );

        if (warehouse.isEmpty()) return;

        Optional<Customer> customer =
                customerRepository.findByCode(
                        CustomerSeeder.SCENARIO_CUSTOMER_CODE
                );

        if (customer.isEmpty()) return;

        Long confirmedId = createIssue(
                warehouse.get().getId(),
                customer.get().getId(),
                LocalDate.now().minusDays(3),
                CONFIRMED_LINES
        );

        if (confirmedId != null) {
            issueService.confirm(confirmedId);
        }

        createIssue(
                warehouse.get().getId(),
                customer.get().getId(),
                LocalDate.now(),
                DRAFT_LINES
        );

        System.out.println("✓ Issue scenario seeded");

    }

    private Long createIssue(
            Long warehouseId,
            Long customerId,
            LocalDate issueDate,
            List<IssueLine> lines
    ) {

        IssueRequest request = new IssueRequest();

        request.setWarehouseId(warehouseId);

        request.setCustomerId(customerId);

        request.setIssueDate(issueDate);

        Long issueId = issueService.create(request).getId();

        int added = 0;

        for (IssueLine line : lines) {

            Optional<Material> material =
                    materialRepository.findByCode(line.materialCode());

            if (material.isEmpty()) continue;

            AddIssueItemRequest item = new AddIssueItemRequest();

            item.setMaterialId(material.get().getId());

            item.setQuantity(new BigDecimal(line.quantity()));

            item.setUnitPrice(material.get().getUnitPrice());

            issueService.addItem(issueId, item);

            added++;

        }

        return added == 0 ? null : issueId;

    }

}
