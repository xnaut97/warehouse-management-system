package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.issue.AddIssueItemRequest;
import com.github.xnaut97.wms.dto.issue.IssueRequest;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.repository.CustomerRepository;
import com.github.xnaut97.wms.repository.RawMaterialRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
import com.github.xnaut97.wms.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Component
@RequiredArgsConstructor
@Transactional
public class IssueSeeder {

    private final IssueService issueService;

    private final GoodsIssueRepository goodsIssueRepository;
    private final GoodsIssueItemRepository goodsIssueItemRepository;

    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final RawMaterialRepository materialRepository;

    public void seed() {

        if (goodsIssueRepository.count() > 0) return;
        if (goodsIssueItemRepository.count() > 0) return;

        if (materialRepository.count() == 0) return;
        if (warehouseRepository.count() == 0) return;
        if (customerRepository.count() == 0) return;


        ThreadLocalRandom random = ThreadLocalRandom.current();

        List<Customer> customers = customerRepository.findAll();

        List<Warehouse> warehouses = warehouseRepository.findAll();

        List<RawMaterial> materials = materialRepository.findAll();

        for (int i = 0; i < 30; i++) {

            IssueRequest request =
                    new IssueRequest();

            request.setWarehouseId(

                    warehouses.get(

                            random.nextInt(

                                    warehouses.size()

                            )

                    ).getId()

            );

            request.setCustomerId(

                    customers.get(

                            random.nextInt(

                                    customers.size()

                            )

                    ).getId()

            );

            request.setIssueDate(

                    LocalDate.now()

                            .minusDays(

                                    random.nextInt(90)

                            )

            );

            Long issueId = issueService.create(request).getId();

            Collections.shuffle(materials);

            int items = random.nextInt(1, materials.size() + 1);

            for (int j = 0; j < items; j++) {

                RawMaterial material = materials.get(j);

                AddIssueItemRequest item = new AddIssueItemRequest();

                item.setMaterialId(material.getId());

                item.setQuantity(BigDecimal.valueOf(random.nextInt(5, 40)));

                item.setUnitPrice(material.getUnitPrice());

                issueService.addItem(issueId, item);

            }

            float confirmChance = random.nextFloat();
            if (confirmChance < 0.5f)
                issueService.confirm(issueId);

        }

        System.out.println("✓ Issue Seeder Completed");

    }

}