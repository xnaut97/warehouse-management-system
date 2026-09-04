package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.enums.AlertType;
import com.github.xnaut97.wms.service.alert.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SeedValidationReporter {

    private record Check(
            String name,
            boolean passed,
            String detail
    ) {
    }

    private static final String PRODUCTION_MOVEMENTS = """
            SELECT pri.product_id pid, pri.lot_number lot, pr.receipt_date d,
                   pri.quantity q
            FROM product_receipts pr
            JOIN product_receipt_items pri ON pri.receipt_id = pr.id
            WHERE pr.status = 'CONFIRMED'
            UNION ALL
            SELECT pii.product_id, pii.lot_number, pi.issue_date, -pii.quantity
            FROM product_issues pi
            JOIN product_issue_items pii ON pii.issue_id = pi.id
            WHERE pi.status = 'CONFIRMED'
            UNION ALL
            SELECT inv.product_id, inv.lot_number, st.stocktaking_date,
                   b.variance_quantity
            FROM stocktaking_item_batches b
            JOIN stocktaking_item si ON si.id = b.item_id
            JOIN stocktaking st ON st.id = si.stocktaking_id
            JOIN product_inventories inv ON inv.id = b.product_inventory_id
            WHERE st.status = 'STOCK_BALANCED'
            """;

    private final JdbcTemplate jdbcTemplate;

    private final AlertService alertService;

    public void report() {

        List<Check> checks = new ArrayList<>();

        checks.add(productCatalog());

        checks.add(packagingCatalog());

        checks.add(bomSpecification());

        checks.add(materialReceiptRules());

        checks.add(negativeInventory());

        checks.add(bomConsistency());

        checks.add(productionConsistency());

        checks.add(lotIntegrity());

        checks.add(fefoConsistency());

        checks.add(inventoryBalance());

        checks.add(costingFlow());

        checks.add(stocktakingSpread());

        checks.add(alertConditions());

        System.out.println("── Seed validation ──");

        for (Check check : checks) {

            System.out.printf(
                    "  %-32s %s  %s%n",
                    check.name(),
                    check.passed() ? "PASS" : "FAIL",
                    check.detail()
            );

        }

    }

    private Check productCatalog() {

        List<String> problems = new ArrayList<>();

        for (ProductSeeder.Recipe recipe : ProductSeeder.RECIPES) {

            Map<String, Object> row = row("""
                    SELECT minimum_stock, maximum_stock, unit, category
                    FROM products WHERE code = ?
                    """, recipe.code());

            if (row == null) {

                problems.add(recipe.code() + " missing");

                continue;

            }

            if (!matches(row.get("minimum_stock"), recipe.minimumStock())
                    || !matches(row.get("maximum_stock"), recipe.maximumStock())) {

                problems.add(recipe.code() + " min/max");

            }

            if (!recipe.category().equals(row.get("category"))) {

                problems.add(recipe.code() + " category");

            }

        }

        return new Check(
                "Product catalog and min/max",
                problems.isEmpty(),
                problems.isEmpty()
                        ? "%d SKUs match".formatted(ProductSeeder.RECIPES.size())
                        : String.join(", ", problems)
        );

    }

    private Check packagingCatalog() {

        List<String> problems = new ArrayList<>();

        for (ProductSeeder.Recipe product : ProductSeeder.RECIPES) {

            Map<String, Object> row = row("""
                    SELECT minimum_stock, maximum_stock, unit
                    FROM materials WHERE code = ?
                    """, product.packagingCode());

            if (row == null) {

                problems.add(product.packagingCode() + " missing");

                continue;

            }

            if (!matches(
                    row.get("minimum_stock"),
                    MaterialSeeder.PACKAGING_MINIMUM_STOCK
            ) || !matches(
                    row.get("maximum_stock"),
                    MaterialSeeder.PACKAGING_MAXIMUM_STOCK
            )) {

                problems.add(product.packagingCode() + " min/max");

            }

            long linked = count("""
                    SELECT COUNT(*) FROM bom_items bi
                    JOIN boms b ON b.id = bi.bom_id
                    JOIN products p ON p.id = b.product_id
                    JOIN materials m ON m.id = bi.raw_material_id
                    WHERE p.code = ? AND m.code = ?
                    """, product.code(), product.packagingCode());

            if (linked != 1) {

                problems.add(product.code() + " packaging not in BOM");

            }

        }

        long shared = count("""
                SELECT COUNT(*) FROM (
                  SELECT bi.raw_material_id
                  FROM bom_items bi
                  JOIN materials m ON m.id = bi.raw_material_id
                  WHERE m.unit = ?
                  GROUP BY 1 HAVING COUNT(DISTINCT bi.bom_id) > 1
                ) x
                """, MaterialSeeder.PIECE_UNIT);

        return new Check(
                "Packaging SKU per product",
                problems.isEmpty() && shared == 0,
                problems.isEmpty()
                        ? "%d packaging SKUs, %d shared"
                        .formatted(ProductSeeder.RECIPES.size(), shared)
                        : String.join(", ", problems)
        );

    }

    private Check bomSpecification() {

        List<String> problems = new ArrayList<>();

        for (BOMSeeder.Formula formula : BOMSeeder.FORMULAS) {

            List<BOMSeeder.Line> expected = BOMSeeder.allLines(formula);

            List<Map<String, Object>> actual = jdbcTemplate.queryForList("""
                    SELECT m.code, bi.consumption_quantity, bi.mixing_ratio,
                           bi.max_waste_ratio
                    FROM boms b
                    JOIN products p ON p.id = b.product_id
                    JOIN bom_items bi ON bi.bom_id = b.id
                    JOIN materials m ON m.id = bi.raw_material_id
                    WHERE b.code = ? AND p.code = ? AND b.enabled = 1
                    """, formula.code(), formula.productCode());

            if (actual.size() != expected.size()) {

                problems.add("%s %d/%d items".formatted(
                        formula.code(), actual.size(), expected.size()));

                continue;

            }

            Map<String, Map<String, Object>> byCode = new LinkedHashMap<>();

            actual.forEach(row -> byCode.put((String) row.get("code"), row));

            for (BOMSeeder.Line line : expected) {

                Map<String, Object> row = byCode.get(line.materialCode());

                if (row == null
                        || !matches(
                        row.get("consumption_quantity"),
                        line.consumptionQuantity()
                )
                        || !matches(
                        row.get("mixing_ratio"),
                        line.mixingRatio()
                )
                        || !matches(
                        row.get("max_waste_ratio"),
                        line.maxWasteRatio()
                )) {

                    problems.add(
                            formula.code() + "/" + line.materialCode()
                    );

                }

            }

        }

        return new Check(
                "BOM matches specification",
                problems.isEmpty(),
                problems.isEmpty()
                        ? "%d BOMs match".formatted(BOMSeeder.FORMULAS.size())
                        : String.join(", ", problems)
        );

    }

    private Check materialReceiptRules() {

        long sand = count("""
                SELECT COUNT(*) FROM goods_receipt_items gri
                JOIN materials m ON m.id = gri.material_id
                WHERE m.code = ? AND MOD(gri.quantity, ?) <> 0
                """, MaterialSeeder.SAND, MaterialSeeder.SAND_CONTAINER);

        long cement = count("""
                SELECT COUNT(*) FROM goods_receipt_items gri
                JOIN materials m ON m.id = gri.material_id
                WHERE m.code = ? AND (gri.quantity < ? OR gri.quantity > ?)
                """,
                MaterialSeeder.CEMENT,
                MaterialSeeder.CEMENT_RECEIPT_MINIMUM,
                MaterialSeeder.CEMENT_RECEIPT_MAXIMUM
        );

        long hpmc = count("""
                SELECT COUNT(*) FROM goods_receipt_items gri
                JOIN materials m ON m.id = gri.material_id
                WHERE m.code = ? AND MOD(gri.quantity, ?) <> 0
                """, MaterialSeeder.HPMC, MaterialSeeder.HPMC_LOT);

        long rdp = count("""
                SELECT COUNT(*) FROM goods_receipt_items gri
                JOIN materials m ON m.id = gri.material_id
                WHERE m.code = ? AND MOD(gri.quantity, ?) <> 0
                """, MaterialSeeder.RDP, MaterialSeeder.RDP_LOT);

        return new Check(
                "Material receipt size rules",
                sand == 0 && cement == 0 && hpmc == 0 && rdp == 0,
                "sand %d, cement %d, hpmc %d, rdp %d off-spec"
                        .formatted(sand, cement, hpmc, rdp)
        );

    }

    private boolean matches(
            Object actual,
            BigDecimal expected
    ) {

        return actual instanceof BigDecimal value
                && value.compareTo(expected) == 0;

    }

    private Map<String, Object> row(
            String sql,
            Object... args
    ) {

        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(sql, args);

        return rows.isEmpty() ? null : rows.getFirst();

    }

    private Check negativeInventory() {

        long materials = count(
                "SELECT COUNT(*) FROM inventories WHERE quantity < 0"
        );

        long products = count(
                "SELECT COUNT(*) FROM product_inventories WHERE quantity < 0"
        );

        return new Check(
                "Negative inventory",
                materials == 0 && products == 0,
                "material rows %d, product rows %d".formatted(materials, products)
        );

    }

    private Check bomConsistency() {

        long breaches = count("""
                WITH prod AS (
                  SELECT pr.receipt_date d, pri.product_id, SUM(pri.quantity) qty
                  FROM product_receipts pr
                  JOIN product_receipt_items pri ON pri.receipt_id = pr.id
                  WHERE pr.status = 'CONFIRMED'
                  GROUP BY 1, 2),
                std AS (
                  SELECT p.d, bi.raw_material_id mid,
                         SUM(p.qty * bi.consumption_quantity) base,
                         SUM(p.qty * bi.consumption_quantity
                             * (1 + bi.max_waste_ratio / 100)) ceiling
                  FROM prod p
                  JOIN boms b ON b.product_id = p.product_id AND b.enabled = 1
                  JOIN bom_items bi ON bi.bom_id = b.id
                  GROUP BY 1, 2),
                act AS (
                  SELECT gi.issue_date d, gii.material_id mid,
                         SUM(gii.quantity) qty
                  FROM goods_issues gi
                  JOIN goods_issue_items gii ON gii.issue_id = gi.id
                  WHERE gi.status = 'CONFIRMED' AND gi.customer_id IS NULL
                  GROUP BY 1, 2)
                SELECT COUNT(*) FROM std JOIN act USING (d, mid)
                WHERE act.qty < std.base - 0.01
                   OR act.qty > std.ceiling + 0.01
                """);

        long compared = count("""
                WITH prod AS (
                  SELECT pr.receipt_date d, pri.product_id
                  FROM product_receipts pr
                  JOIN product_receipt_items pri ON pri.receipt_id = pr.id
                  WHERE pr.status = 'CONFIRMED'
                  GROUP BY 1, 2)
                SELECT COUNT(*) FROM (
                  SELECT p.d, bi.raw_material_id
                  FROM prod p
                  JOIN boms b ON b.product_id = p.product_id AND b.enabled = 1
                  JOIN bom_items bi ON bi.bom_id = b.id
                  GROUP BY 1, 2
                ) x
                """);

        return new Check(
                "BOM vs actual within wastage",
                breaches == 0 && compared > 0,
                "%d breaches over %d material-days".formatted(breaches, compared)
        );

    }

    private Check productionConsistency() {

        long withoutIssue = count("""
                SELECT COUNT(*) FROM (
                  SELECT pr.receipt_date d
                  FROM product_receipts pr
                  WHERE pr.status = 'CONFIRMED'
                  GROUP BY 1
                ) r
                LEFT JOIN (
                  SELECT gi.issue_date d
                  FROM goods_issues gi
                  WHERE gi.status = 'CONFIRMED' AND gi.customer_id IS NULL
                  GROUP BY 1
                ) i ON i.d = r.d
                WHERE i.d IS NULL
                """);

        long outOfRange = count("""
                SELECT COUNT(*) FROM (
                  SELECT pr.receipt_date d, SUM(pri.quantity) qty
                  FROM product_receipts pr
                  JOIN product_receipt_items pri ON pri.receipt_id = pr.id
                  WHERE pr.status = 'CONFIRMED'
                  GROUP BY 1
                ) x WHERE qty < 800 OR qty > 900
                """);

        BigDecimal premiumShare = scalar("""
                SELECT ROUND(100 * SUM(CASE WHEN p.category = 'Keo 2'
                                            THEN pri.quantity ELSE 0 END)
                             / NULLIF(SUM(pri.quantity), 0), 2)
                FROM product_receipt_items pri
                JOIN products p ON p.id = pri.product_id
                JOIN product_receipts pr ON pr.id = pri.receipt_id
                WHERE pr.status = 'CONFIRMED'
                """);

        boolean shareOk = premiumShare != null
                && premiumShare.doubleValue() >= 5
                && premiumShare.doubleValue() <= 10;

        return new Check(
                "Production plan and C1/C2 mix",
                withoutIssue == 0 && outOfRange == 0 && shareOk,
                "%d days without issue, %d days off 800-900, C2 %s%%"
                        .formatted(withoutIssue, outOfRange, premiumShare)
        );

    }

    private Check lotIntegrity() {

        long missingLot = count("""
                SELECT COUNT(*) FROM product_receipt_items
                WHERE lot_number IS NULL OR lot_number = ''
                """);

        long badExpiry = count("""
                SELECT COUNT(*)
                FROM product_receipt_items pri
                JOIN product_receipts pr ON pr.id = pri.receipt_id
                WHERE pri.expiration_date
                      <> DATE_ADD(pr.receipt_date, INTERVAL 1 YEAR)
                """);

        return new Check(
                "Lot number and expiry NSX+1y",
                missingLot == 0 && badExpiry == 0,
                "%d without lot, %d wrong expiry".formatted(missingLot, badExpiry)
        );

    }

    private Check fefoConsistency() {

        long skipped = count("""
                WITH mv AS (%s),
                bal AS (
                  SELECT pid, lot, d,
                         SUM(SUM(q)) OVER (PARTITION BY pid, lot ORDER BY d) b
                  FROM mv GROUP BY pid, lot, d),
                lotexp AS (
                  SELECT product_id pid, lot_number lot,
                         MAX(expiration_date) exp
                  FROM product_inventories GROUP BY 1, 2),
                day_max AS (
                  SELECT pii.product_id pid, pi.issue_date d, MAX(l.exp) max_exp
                  FROM product_issues pi
                  JOIN product_issue_items pii ON pii.issue_id = pi.id
                  JOIN lotexp l ON l.pid = pii.product_id
                                AND l.lot = pii.lot_number
                  WHERE pi.status = 'CONFIRMED'
                  GROUP BY 1, 2)
                SELECT COUNT(DISTINCT l.lot)
                FROM day_max dm
                JOIN lotexp l ON l.pid = dm.pid AND l.exp < dm.max_exp
                JOIN bal b ON b.pid = l.pid AND b.lot = l.lot
                          AND b.d = (SELECT MAX(b2.d) FROM bal b2
                                     WHERE b2.pid = l.pid AND b2.lot = l.lot
                                       AND b2.d <= dm.d)
                WHERE b.b > 0.001
                """.formatted(PRODUCTION_MOVEMENTS));

        long reserved = count("""
                SELECT COUNT(*) FROM product_inventories
                WHERE quantity > 0
                  AND expiration_date IS NOT NULL
                  AND expiration_date
                      BETWEEN DATE_ADD(CURDATE(), INTERVAL 60 DAY)
                          AND DATE_ADD(CURDATE(), INTERVAL 90 DAY)
                """);

        return new Check(
                "FEFO consumption order",
                skipped <= reserved,
                "%d lots skipped, %d near-expiry lots reserved"
                        .formatted(skipped, reserved)
        );

    }

    private Check inventoryBalance() {

        long products = count("""
                WITH mv AS (%s)
                SELECT COUNT(*) FROM (
                  SELECT inv.quantity actual,
                         COALESCE((SELECT SUM(m.q) FROM mv m
                                   WHERE m.pid = inv.product_id
                                     AND m.lot = inv.lot_number), 0) derived
                  FROM product_inventories inv
                ) x WHERE ABS(actual - derived) > 0.011
                """.formatted(PRODUCTION_MOVEMENTS));

        long materials = count("""
                SELECT COUNT(*) FROM (
                  SELECT i.quantity actual,
                    COALESCE((SELECT SUM(gri.quantity)
                              FROM goods_receipt_items gri
                              JOIN goods_receipts gr ON gr.id = gri.receipt_id
                              WHERE gr.status = 'CONFIRMED'
                                AND gr.warehouse_id = i.warehouse_id
                                AND gri.material_id = i.material_id), 0)
                  - COALESCE((SELECT SUM(gii.quantity)
                              FROM goods_issue_items gii
                              JOIN goods_issues gi ON gi.id = gii.issue_id
                              WHERE gi.status = 'CONFIRMED'
                                AND gi.warehouse_id = i.warehouse_id
                                AND gii.material_id = i.material_id), 0)
                  + COALESCE((SELECT SUM(si.variance_quantity)
                              FROM stocktaking_item si
                              JOIN stocktaking st ON st.id = si.stocktaking_id
                              WHERE st.status = 'STOCK_BALANCED'
                                AND st.warehouse_id = i.warehouse_id
                                AND si.material_id = i.material_id), 0) derived
                  FROM inventories i
                ) x WHERE ABS(actual - derived) > 0.011
                """);

        return new Check(
                "Inventory balance equation",
                products == 0 && materials == 0,
                "%d product lots, %d material rows off"
                        .formatted(products, materials)
        );

    }

    private Check costingFlow() {

        long unpriced = count("""
                SELECT COUNT(*) FROM materials m
                WHERE m.unit_price <= 0
                  AND EXISTS (SELECT 1 FROM goods_receipt_items gri
                              JOIN goods_receipts gr ON gr.id = gri.receipt_id
                              WHERE gri.material_id = m.id
                                AND gr.status = 'CONFIRMED')
                """);

        long flatCost = count("""
                SELECT COUNT(*) FROM (
                  SELECT pri.product_id
                  FROM product_receipt_items pri
                  JOIN product_receipts pr ON pr.id = pri.receipt_id
                  WHERE pr.status = 'CONFIRMED'
                  GROUP BY pri.product_id
                  HAVING COUNT(DISTINCT pri.unit_price) < 2
                ) x
                """);

        long noAverage = count("""
                SELECT COUNT(*) FROM products p
                WHERE p.average_price <= 0
                  AND EXISTS (SELECT 1 FROM product_issue_items pii
                              JOIN product_issues pi ON pi.id = pii.issue_id
                              WHERE pii.product_id = p.id
                                AND pi.status = 'CONFIRMED')
                """);

        return new Check(
                "Costing chain not hardcoded",
                unpriced == 0 && flatCost == 0 && noAverage == 0,
                "%d materials unpriced, %d flat-cost SKUs, %d without average"
                        .formatted(unpriced, flatCost, noAverage)
        );

    }

    private Check stocktakingSpread() {

        long monthsInRange = count("""
                SELECT COUNT(*) FROM (
                  SELECT DATE_FORMAT(stocktaking_date, '%Y-%m') ym
                  FROM stocktaking GROUP BY 1 HAVING COUNT(*) BETWEEN 4 AND 8
                ) x
                """);

        long months = count("""
                SELECT COUNT(*) FROM (
                  SELECT DATE_FORMAT(stocktaking_date, '%Y-%m') ym
                  FROM stocktaking GROUP BY 1
                ) x
                """);

        long discrepancies = count("""
                SELECT COUNT(*) FROM stocktaking_item
                WHERE item_status = 'DISCREPANCY'
                """);

        long matched = count("""
                SELECT COUNT(*) FROM stocktaking_item
                WHERE item_status = 'MATCHED'
                """);

        return new Check(
                "Stocktaking coverage",
                discrepancies > 0 && matched > discrepancies,
                "%d/%d months in 4-8 range, %d matched, %d discrepancies"
                        .formatted(monthsInRange, months, matched, discrepancies)
        );

    }

    private Check alertConditions() {

        long belowMin = alertCount(AlertType.BELOW_MIN);

        long aboveMax = alertCount(AlertType.ABOVE_MAX);

        long nearExpiry = alertCount(AlertType.NEAR_EXPIRY);

        long variance = alertCount(AlertType.STOCKTAKING_VARIANCE);

        return new Check(
                "Alert demo conditions",
                belowMin >= 2 && aboveMax >= 1
                        && nearExpiry >= 1 && variance >= 1,
                "below-min %d, above-max %d, near-expiry %d, variance %d"
                        .formatted(belowMin, aboveMax, nearExpiry, variance)
        );

    }

    private long alertCount(AlertType type) {

        return alertService.getAlerts(null, type).getAlerts().size();

    }

    private long count(
            String sql,
            Object... args
    ) {

        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);

        return value == null ? 0 : value;

    }

    private BigDecimal scalar(String sql) {

        return jdbcTemplate.queryForObject(sql, BigDecimal.class);

    }

}
