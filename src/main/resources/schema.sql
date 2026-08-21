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

SET @suppliers_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'suppliers');

SET @suppliers_supplier_group_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'suppliers' AND COLUMN_NAME = 'supplier_group');

SET @stmt := IF(@suppliers_table_exists > 0 AND @suppliers_supplier_group_exists = 0, 'ALTER TABLE suppliers ADD COLUMN supplier_group VARCHAR(32) NULL', 'SELECT 1');
PREPARE suppliers_supplier_group_add FROM @stmt;
EXECUTE suppliers_supplier_group_add;
DEALLOCATE PREPARE suppliers_supplier_group_add;

SET @stmt := IF(@suppliers_table_exists > 0, 'UPDATE suppliers SET supplier_group = ''SAND'' WHERE supplier_group IS NULL OR supplier_group NOT IN (''SAND'', ''CEMENT'', ''ADDITIVE'', ''PACKAGING_MATERIAL'')', 'SELECT 1');
PREPARE suppliers_supplier_group_backfill FROM @stmt;
EXECUTE suppliers_supplier_group_backfill;
DEALLOCATE PREPARE suppliers_supplier_group_backfill;

SET @suppliers_supplier_group_nullable := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'suppliers' AND COLUMN_NAME = 'supplier_group' AND IS_NULLABLE = 'YES');

SET @stmt := IF(@suppliers_supplier_group_nullable > 0, 'ALTER TABLE suppliers MODIFY COLUMN supplier_group VARCHAR(32) NOT NULL', 'SELECT 1');
PREPARE suppliers_supplier_group_require FROM @stmt;
EXECUTE suppliers_supplier_group_require;
DEALLOCATE PREPARE suppliers_supplier_group_require;

SET @suppliers_note_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'suppliers' AND COLUMN_NAME = 'note');

SET @stmt := IF(@suppliers_table_exists > 0 AND @suppliers_note_exists = 0, 'ALTER TABLE suppliers ADD COLUMN note VARCHAR(1000) NULL', 'SELECT 1');
PREPARE suppliers_note_add FROM @stmt;
EXECUTE suppliers_note_add;
DEALLOCATE PREPARE suppliers_note_add;

SET @customers_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers');

SET @customers_code_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'code');

SET @stmt := IF(@customers_table_exists > 0 AND @customers_code_exists = 0, 'ALTER TABLE customers ADD COLUMN code VARCHAR(30) NULL', 'SELECT 1');
PREPARE customers_code_add FROM @stmt;
EXECUTE customers_code_add;
DEALLOCATE PREPARE customers_code_add;

SET @stmt := IF(@customers_table_exists > 0, 'UPDATE customers SET code = CONCAT(''KH'', LPAD(id, 4, ''0'')) WHERE code IS NULL OR code = ''''', 'SELECT 1');
PREPARE customers_code_backfill FROM @stmt;
EXECUTE customers_code_backfill;
DEALLOCATE PREPARE customers_code_backfill;

SET @customers_code_nullable := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'code' AND IS_NULLABLE = 'YES');

SET @stmt := IF(@customers_code_nullable > 0, 'ALTER TABLE customers MODIFY COLUMN code VARCHAR(30) NOT NULL', 'SELECT 1');
PREPARE customers_code_require FROM @stmt;
EXECUTE customers_code_require;
DEALLOCATE PREPARE customers_code_require;

SET @customers_code_indexed := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'code');

SET @stmt := IF(@customers_table_exists > 0 AND @customers_code_indexed = 0, 'ALTER TABLE customers ADD CONSTRAINT uk_customers_code UNIQUE (code)', 'SELECT 1');
PREPARE customers_code_unique FROM @stmt;
EXECUTE customers_code_unique;
DEALLOCATE PREPARE customers_code_unique;

SET @customers_customer_group_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'customer_group');

SET @stmt := IF(@customers_table_exists > 0 AND @customers_customer_group_exists = 0, 'ALTER TABLE customers ADD COLUMN customer_group VARCHAR(32) NULL', 'SELECT 1');
PREPARE customers_customer_group_add FROM @stmt;
EXECUTE customers_customer_group_add;
DEALLOCATE PREPARE customers_customer_group_add;

SET @stmt := IF(@customers_table_exists > 0, 'UPDATE customers SET customer_group = ''RETAIL'' WHERE customer_group IS NULL OR customer_group NOT IN (''AGENT'', ''PROJECT'', ''RETAIL'')', 'SELECT 1');
PREPARE customers_customer_group_backfill FROM @stmt;
EXECUTE customers_customer_group_backfill;
DEALLOCATE PREPARE customers_customer_group_backfill;

SET @customers_customer_group_nullable := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'customer_group' AND IS_NULLABLE = 'YES');

SET @stmt := IF(@customers_customer_group_nullable > 0, 'ALTER TABLE customers MODIFY COLUMN customer_group VARCHAR(32) NOT NULL', 'SELECT 1');
PREPARE customers_customer_group_require FROM @stmt;
EXECUTE customers_customer_group_require;
DEALLOCATE PREPARE customers_customer_group_require;

SET @customers_receiver_name_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'receiver_name');

SET @stmt := IF(@customers_table_exists > 0 AND @customers_receiver_name_exists = 0, 'ALTER TABLE customers ADD COLUMN receiver_name VARCHAR(100) NULL', 'SELECT 1');
PREPARE customers_receiver_name_add FROM @stmt;
EXECUTE customers_receiver_name_add;
DEALLOCATE PREPARE customers_receiver_name_add;

SET @customers_note_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'note');

SET @stmt := IF(@customers_table_exists > 0 AND @customers_note_exists = 0, 'ALTER TABLE customers ADD COLUMN note VARCHAR(1000) NULL', 'SELECT 1');
PREPARE customers_note_add FROM @stmt;
EXECUTE customers_note_add;
DEALLOCATE PREPARE customers_note_add;

SET @customers_email_required := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'email' AND IS_NULLABLE = 'NO');

SET @stmt := IF(@customers_email_required > 0, 'ALTER TABLE customers MODIFY COLUMN email VARCHAR(255) NULL', 'SELECT 1');
PREPARE customers_email_relax FROM @stmt;
EXECUTE customers_email_relax;
DEALLOCATE PREPARE customers_email_relax;
