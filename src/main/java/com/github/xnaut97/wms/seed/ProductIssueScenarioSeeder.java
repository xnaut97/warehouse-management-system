package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.product.issue.AddProductIssueItemRequest;
import com.github.xnaut97.wms.dto.product.issue.ProductIssueRequest;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.repository.CustomerRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.product.ProductIssueRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import com.github.xnaut97.wms.service.product.ProductIssueService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductIssueScenarioSeeder {

    private record IssueLine(
            String productCode,
            String quantity,
            String unitPrice,
            String lotNumber
    ) {
    }

    private static final String SEED_USERNAME = "admin";

    private static final List<IssueLine> CONFIRMED_LINES = List.of(
            new IssueLine("FP004", "100", "780000", "LOT-FP004-A")
    );

    private static final List<IssueLine> DRAFT_LINES = List.of(
            new IssueLine("FP001", "50", "2150000", "LOT-FP001-A"),
            new IssueLine("FP001", "20", "2150000", "LOT-FP001-B"),
            new IssueLine("FP002", "30", "2800000", "LOT-FP002-A")
    );

    private final ProductIssueService productIssueService;

    private final ProductIssueRepository productIssueRepository;

    private final ProductInventoryRepository productInventoryRepository;

    private final ProductRepository productRepository;

    private final WarehouseRepository warehouseRepository;

    private final CustomerRepository customerRepository;

    private final UserRepository userRepository;

    @Transactional
    public void seed() {

        if (productIssueRepository.existsByCustomerCode(
                CustomerSeeder.SCENARIO_CUSTOMER_CODE
        )) {
            return;
        }

        if (userRepository.findByUsername(SEED_USERNAME).isEmpty()) return;

        Optional<Warehouse> warehouse =
                warehouseRepository.findByCode(
                        WarehouseService.PRODUCT_WAREHOUSE_CODE
                );

        if (warehouse.isEmpty()) return;

        Optional<Customer> customer =
                customerRepository.findByCode(
                        CustomerSeeder.SCENARIO_CUSTOMER_CODE
                );

        if (customer.isEmpty()) return;

        Optional<Product> anchor =
                productRepository.findByCode(
                        ProductStockScenarioSeeder.ANCHOR_PRODUCT_CODE
                );

        if (anchor.isEmpty()) return;

        boolean stockReady = productInventoryRepository
                .findByWarehouseProductAndLot(
                        warehouse.get().getId(),
                        anchor.get().getId(),
                        ProductStockScenarioSeeder.ANCHOR_LOT_NUMBER
                )
                .isPresent();

        if (!stockReady) return;

        runAsSeedUser(() -> {

            Long confirmedId = createIssue(
                    warehouse.get().getId(),
                    customer.get().getId(),
                    LocalDate.now().minusDays(2),
                    CONFIRMED_LINES
            );

            if (confirmedId != null) {
                productIssueService.confirm(confirmedId);
            }

            createIssue(
                    warehouse.get().getId(),
                    customer.get().getId(),
                    LocalDate.now(),
                    DRAFT_LINES
            );

        });

        System.out.println("✓ Product issue scenario seeded");

    }

    private Long createIssue(
            Long warehouseId,
            Long customerId,
            LocalDate issueDate,
            List<IssueLine> lines
    ) {

        ProductIssueRequest request = new ProductIssueRequest();

        request.setWarehouseId(warehouseId);

        request.setCustomerId(customerId);

        request.setIssueDate(issueDate);

        Long issueId =
                productIssueService.create(request).getId();

        int added = 0;

        for (IssueLine line : lines) {

            Optional<Product> product =
                    productRepository.findByCode(line.productCode());

            if (product.isEmpty()) continue;

            AddProductIssueItemRequest item =
                    new AddProductIssueItemRequest();

            item.setProductId(product.get().getId());

            item.setQuantity(new BigDecimal(line.quantity()));

            item.setUnitPrice(new BigDecimal(line.unitPrice()));

            item.setLotNumber(line.lotNumber());

            productIssueService.addItem(issueId, item);

            added++;

        }

        return added == 0 ? null : issueId;

    }

    private void runAsSeedUser(Runnable action) {

        SecurityContext previous =
                SecurityContextHolder.getContext();

        try {

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            SEED_USERNAME,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    )
            );

            SecurityContextHolder.setContext(context);

            action.run();

        } finally {

            SecurityContextHolder.setContext(previous);

        }

    }

}
