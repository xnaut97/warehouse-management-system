SET @stocktaking_status_is_enum := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stocktaking' AND COLUMN_NAME = 'status' AND DATA_TYPE = 'enum');

SET @stmt := IF(@stocktaking_status_is_enum > 0, 'ALTER TABLE stocktaking MODIFY COLUMN status VARCHAR(32) NOT NULL', 'SELECT 1');
PREPARE stocktaking_status_widen FROM @stmt;
EXECUTE stocktaking_status_widen;
DEALLOCATE PREPARE stocktaking_status_widen;

SET @stmt := IF(@stocktaking_status_is_enum > 0, 'UPDATE stocktaking SET status = ''IN_PROGRESS'' WHERE status = ''DRAFT''', 'SELECT 1');
PREPARE stocktaking_status_draft FROM @stmt;
EXECUTE stocktaking_status_draft;
DEALLOCATE PREPARE stocktaking_status_draft;

SET @stmt := IF(@stocktaking_status_is_enum > 0, 'UPDATE stocktaking SET status = ''STOCK_BALANCED'' WHERE status = ''CONFIRMED''', 'SELECT 1');
PREPARE stocktaking_status_confirmed FROM @stmt;
EXECUTE stocktaking_status_confirmed;
DEALLOCATE PREPARE stocktaking_status_confirmed;

SET @stocktaking_item_material_required := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stocktaking_item' AND COLUMN_NAME = 'material_id' AND IS_NULLABLE = 'NO');

SET @stmt := IF(@stocktaking_item_material_required > 0, 'ALTER TABLE stocktaking_item MODIFY COLUMN material_id BIGINT NULL', 'SELECT 1');
PREPARE stocktaking_item_material_relax FROM @stmt;
EXECUTE stocktaking_item_material_relax;
DEALLOCATE PREPARE stocktaking_item_material_relax;

SET @stocktaking_item_physical_required := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stocktaking_item' AND COLUMN_NAME = 'physical_quantity' AND IS_NULLABLE = 'NO');

SET @stmt := IF(@stocktaking_item_physical_required > 0, 'ALTER TABLE stocktaking_item MODIFY COLUMN physical_quantity DECIMAL(18,2) NULL', 'SELECT 1');
PREPARE stocktaking_item_physical_relax FROM @stmt;
EXECUTE stocktaking_item_physical_relax;
DEALLOCATE PREPARE stocktaking_item_physical_relax;

SET @products_selling_price_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'products' AND COLUMN_NAME = 'selling_price');

SET @stmt := IF(@products_selling_price_exists > 0, 'UPDATE products SET average_price = selling_price WHERE average_price IS NULL OR average_price = 0', 'SELECT 1');
PREPARE products_average_price_backfill FROM @stmt;
EXECUTE products_average_price_backfill;
DEALLOCATE PREPARE products_average_price_backfill;

SET @stmt := IF(@products_selling_price_exists > 0, 'ALTER TABLE products DROP COLUMN selling_price', 'SELECT 1');
PREPARE products_selling_price_drop FROM @stmt;
EXECUTE products_selling_price_drop;
DEALLOCATE PREPARE products_selling_price_drop;

SET @products_category_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'products' AND COLUMN_NAME = 'category');

SET @stmt := IF(@products_category_exists > 0, 'UPDATE products SET category = ''Keo 2'' WHERE category LIKE ''%2%'' AND category <> ''Keo 2''', 'SELECT 1');
PREPARE products_category_keo2 FROM @stmt;
EXECUTE products_category_keo2;
DEALLOCATE PREPARE products_category_keo2;

SET @stmt := IF(@products_category_exists > 0, 'UPDATE products SET category = ''Keo C1'' WHERE category IS NULL OR TRIM(category) = '''' OR category NOT IN (''Keo C1'', ''Keo 2'')', 'SELECT 1');
PREPARE products_category_keoc1 FROM @stmt;
EXECUTE products_category_keoc1;
DEALLOCATE PREPARE products_category_keoc1;

SET @role_role_is_enum := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'role' AND COLUMN_NAME = 'role' AND DATA_TYPE = 'enum');

SET @stmt := IF(@role_role_is_enum > 0, 'ALTER TABLE role MODIFY COLUMN role VARCHAR(32) NOT NULL', 'SELECT 1');
PREPARE role_role_widen FROM @stmt;
EXECUTE role_role_widen;
DEALLOCATE PREPARE role_role_widen;
