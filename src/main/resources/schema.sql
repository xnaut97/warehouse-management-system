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

SET @gri_raw_material_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'goods_receipt_items' AND REFERENCED_TABLE_NAME = 'raw_materials' LIMIT 1);

SET @stmt := IF(@gri_raw_material_fk IS NOT NULL, CONCAT('ALTER TABLE goods_receipt_items DROP FOREIGN KEY `', @gri_raw_material_fk, '`'), 'SELECT 1');
PREPARE gri_raw_material_fk_drop FROM @stmt;
EXECUTE gri_raw_material_fk_drop;
DEALLOCATE PREPARE gri_raw_material_fk_drop;

SET @gii_raw_material_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'goods_issue_items' AND REFERENCED_TABLE_NAME = 'raw_materials' LIMIT 1);

SET @stmt := IF(@gii_raw_material_fk IS NOT NULL, CONCAT('ALTER TABLE goods_issue_items DROP FOREIGN KEY `', @gii_raw_material_fk, '`'), 'SELECT 1');
PREPARE gii_raw_material_fk_drop FROM @stmt;
EXECUTE gii_raw_material_fk_drop;
DEALLOCATE PREPARE gii_raw_material_fk_drop;

SET @it_raw_material_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'inventory_transactions' AND REFERENCED_TABLE_NAME = 'raw_materials' LIMIT 1);

SET @stmt := IF(@it_raw_material_fk IS NOT NULL, CONCAT('ALTER TABLE inventory_transactions DROP FOREIGN KEY `', @it_raw_material_fk, '`'), 'SELECT 1');
PREPARE it_raw_material_fk_drop FROM @stmt;
EXECUTE it_raw_material_fk_drop;
DEALLOCATE PREPARE it_raw_material_fk_drop;

SET @inv_raw_material_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'inventories' AND REFERENCED_TABLE_NAME = 'raw_materials' LIMIT 1);

SET @stmt := IF(@inv_raw_material_fk IS NOT NULL, CONCAT('ALTER TABLE inventories DROP FOREIGN KEY `', @inv_raw_material_fk, '`'), 'SELECT 1');
PREPARE inv_raw_material_fk_drop FROM @stmt;
EXECUTE inv_raw_material_fk_drop;
DEALLOCATE PREPARE inv_raw_material_fk_drop;

SET @sti_raw_material_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'stocktaking_item' AND REFERENCED_TABLE_NAME = 'raw_materials' LIMIT 1);

SET @stmt := IF(@sti_raw_material_fk IS NOT NULL, CONCAT('ALTER TABLE stocktaking_item DROP FOREIGN KEY `', @sti_raw_material_fk, '`'), 'SELECT 1');
PREPARE sti_raw_material_fk_drop FROM @stmt;
EXECUTE sti_raw_material_fk_drop;
DEALLOCATE PREPARE sti_raw_material_fk_drop;

SET @bi_raw_material_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'bom_items' AND REFERENCED_TABLE_NAME = 'raw_materials' LIMIT 1);

SET @stmt := IF(@bi_raw_material_fk IS NOT NULL, CONCAT('ALTER TABLE bom_items DROP FOREIGN KEY `', @bi_raw_material_fk, '`'), 'SELECT 1');
PREPARE bi_raw_material_fk_drop FROM @stmt;
EXECUTE bi_raw_material_fk_drop;
DEALLOCATE PREPARE bi_raw_material_fk_drop;

SET @pri_finished_product_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'product_receipt_items' AND REFERENCED_TABLE_NAME = 'finished_products' LIMIT 1);

SET @stmt := IF(@pri_finished_product_fk IS NOT NULL, CONCAT('ALTER TABLE product_receipt_items DROP FOREIGN KEY `', @pri_finished_product_fk, '`'), 'SELECT 1');
PREPARE pri_finished_product_fk_drop FROM @stmt;
EXECUTE pri_finished_product_fk_drop;
DEALLOCATE PREPARE pri_finished_product_fk_drop;

SET @pii_finished_product_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'product_issue_items' AND REFERENCED_TABLE_NAME = 'finished_products' LIMIT 1);

SET @stmt := IF(@pii_finished_product_fk IS NOT NULL, CONCAT('ALTER TABLE product_issue_items DROP FOREIGN KEY `', @pii_finished_product_fk, '`'), 'SELECT 1');
PREPARE pii_finished_product_fk_drop FROM @stmt;
EXECUTE pii_finished_product_fk_drop;
DEALLOCATE PREPARE pii_finished_product_fk_drop;

SET @pinv_finished_product_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'product_inventories' AND REFERENCED_TABLE_NAME = 'finished_products' LIMIT 1);

SET @stmt := IF(@pinv_finished_product_fk IS NOT NULL, CONCAT('ALTER TABLE product_inventories DROP FOREIGN KEY `', @pinv_finished_product_fk, '`'), 'SELECT 1');
PREPARE pinv_finished_product_fk_drop FROM @stmt;
EXECUTE pinv_finished_product_fk_drop;
DEALLOCATE PREPARE pinv_finished_product_fk_drop;

SET @sti_finished_product_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'stocktaking_item' AND REFERENCED_TABLE_NAME = 'finished_products' LIMIT 1);

SET @stmt := IF(@sti_finished_product_fk IS NOT NULL, CONCAT('ALTER TABLE stocktaking_item DROP FOREIGN KEY `', @sti_finished_product_fk, '`'), 'SELECT 1');
PREPARE sti_finished_product_fk_drop FROM @stmt;
EXECUTE sti_finished_product_fk_drop;
DEALLOCATE PREPARE sti_finished_product_fk_drop;

SET @bom_finished_product_fk := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'boms' AND REFERENCED_TABLE_NAME = 'finished_products' LIMIT 1);

SET @stmt := IF(@bom_finished_product_fk IS NOT NULL, CONCAT('ALTER TABLE boms DROP FOREIGN KEY `', @bom_finished_product_fk, '`'), 'SELECT 1');
PREPARE bom_finished_product_fk_drop FROM @stmt;
EXECUTE bom_finished_product_fk_drop;
DEALLOCATE PREPARE bom_finished_product_fk_drop;

-- Migrate warehouse inventory and movements quantity columns to integer (BIGINT)
SET @inv_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inventories');
SET @stmt := IF(@inv_table_exists > 0, 'UPDATE inventories SET quantity = ROUND(quantity, 0)', 'SELECT 1');
PREPARE inv_round FROM @stmt;
EXECUTE inv_round;
DEALLOCATE PREPARE inv_round;
SET @stmt := IF(@inv_table_exists > 0, 'ALTER TABLE inventories MODIFY COLUMN quantity BIGINT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE inv_widen FROM @stmt;
EXECUTE inv_widen;
DEALLOCATE PREPARE inv_widen;

SET @pinv_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_inventories');
SET @stmt := IF(@pinv_table_exists > 0, 'UPDATE product_inventories SET quantity = ROUND(quantity, 0)', 'SELECT 1');
PREPARE pinv_round FROM @stmt;
EXECUTE pinv_round;
DEALLOCATE PREPARE pinv_round;
SET @stmt := IF(@pinv_table_exists > 0, 'ALTER TABLE product_inventories MODIFY COLUMN quantity BIGINT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE pinv_widen FROM @stmt;
EXECUTE pinv_widen;
DEALLOCATE PREPARE pinv_widen;

SET @it_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inventory_transactions');
SET @stmt := IF(@it_table_exists > 0, 'UPDATE inventory_transactions SET quantity = ROUND(quantity, 0)', 'SELECT 1');
PREPARE it_round FROM @stmt;
EXECUTE it_round;
DEALLOCATE PREPARE it_round;
SET @stmt := IF(@it_table_exists > 0, 'ALTER TABLE inventory_transactions MODIFY COLUMN quantity BIGINT NOT NULL', 'SELECT 1');
PREPARE it_widen FROM @stmt;
EXECUTE it_widen;
DEALLOCATE PREPARE it_widen;

SET @gri_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'goods_receipt_items');
SET @stmt := IF(@gri_table_exists > 0, 'UPDATE goods_receipt_items SET quantity = ROUND(quantity, 0)', 'SELECT 1');
PREPARE gri_round FROM @stmt;
EXECUTE gri_round;
DEALLOCATE PREPARE gri_round;
SET @stmt := IF(@gri_table_exists > 0, 'ALTER TABLE goods_receipt_items MODIFY COLUMN quantity BIGINT NOT NULL', 'SELECT 1');
PREPARE gri_widen FROM @stmt;
EXECUTE gri_widen;
DEALLOCATE PREPARE gri_widen;

SET @gii_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'goods_issue_items');
SET @stmt := IF(@gii_table_exists > 0, 'UPDATE goods_issue_items SET quantity = ROUND(quantity, 0)', 'SELECT 1');
PREPARE gii_round FROM @stmt;
EXECUTE gii_round;
DEALLOCATE PREPARE gii_round;
SET @stmt := IF(@gii_table_exists > 0, 'ALTER TABLE goods_issue_items MODIFY COLUMN quantity BIGINT NOT NULL', 'SELECT 1');
PREPARE gii_widen FROM @stmt;
EXECUTE gii_widen;
DEALLOCATE PREPARE gii_widen;

SET @pri_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_receipt_items');
SET @stmt := IF(@pri_table_exists > 0, 'UPDATE product_receipt_items SET quantity = ROUND(quantity, 0)', 'SELECT 1');
PREPARE pri_round FROM @stmt;
EXECUTE pri_round;
DEALLOCATE PREPARE pri_round;
SET @stmt := IF(@pri_table_exists > 0, 'ALTER TABLE product_receipt_items MODIFY COLUMN quantity BIGINT NOT NULL', 'SELECT 1');
PREPARE pri_widen FROM @stmt;
EXECUTE pri_widen;
DEALLOCATE PREPARE pri_widen;

SET @pii_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_issue_items');
SET @stmt := IF(@pii_table_exists > 0, 'UPDATE product_issue_items SET quantity = ROUND(quantity, 0)', 'SELECT 1');
PREPARE pii_round FROM @stmt;
EXECUTE pii_round;
DEALLOCATE PREPARE pii_round;
SET @stmt := IF(@pii_table_exists > 0, 'ALTER TABLE product_issue_items MODIFY COLUMN quantity BIGINT NOT NULL', 'SELECT 1');
PREPARE pii_widen FROM @stmt;
EXECUTE pii_widen;
DEALLOCATE PREPARE pii_widen;

SET @sti_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stocktaking_item');
SET @stmt := IF(@sti_table_exists > 0, 'UPDATE stocktaking_item SET system_quantity = ROUND(system_quantity, 0), physical_quantity = ROUND(physical_quantity, 0), variance_quantity = ROUND(variance_quantity, 0)', 'SELECT 1');
PREPARE sti_round FROM @stmt;
EXECUTE sti_round;
DEALLOCATE PREPARE sti_round;
SET @stmt := IF(@sti_table_exists > 0, 'ALTER TABLE stocktaking_item MODIFY COLUMN system_quantity BIGINT NOT NULL, MODIFY COLUMN physical_quantity BIGINT NULL, MODIFY COLUMN variance_quantity BIGINT NOT NULL', 'SELECT 1');
PREPARE sti_widen FROM @stmt;
EXECUTE sti_widen;
DEALLOCATE PREPARE sti_widen;

SET @stib_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stocktaking_item_batches');
SET @stmt := IF(@stib_table_exists > 0, 'UPDATE stocktaking_item_batches SET system_quantity = ROUND(system_quantity, 0), physical_quantity = ROUND(physical_quantity, 0), variance_quantity = ROUND(variance_quantity, 0)', 'SELECT 1');
PREPARE stib_round FROM @stmt;
EXECUTE stib_round;
DEALLOCATE PREPARE stib_round;
SET @stmt := IF(@stib_table_exists > 0, 'ALTER TABLE stocktaking_item_batches MODIFY COLUMN system_quantity BIGINT NOT NULL, MODIFY COLUMN physical_quantity BIGINT NULL, MODIFY COLUMN variance_quantity BIGINT NOT NULL', 'SELECT 1');
PREPARE stib_widen FROM @stmt;
EXECUTE stib_widen;
DEALLOCATE PREPARE stib_widen;

SET @materials_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'materials');
SET @stmt := IF(@materials_table_exists > 0, 'UPDATE materials SET minimum_stock = ROUND(minimum_stock, 0), maximum_stock = ROUND(maximum_stock, 0)', 'SELECT 1');
PREPARE materials_round FROM @stmt;
EXECUTE materials_round;
DEALLOCATE PREPARE materials_round;
SET @stmt := IF(@materials_table_exists > 0, 'ALTER TABLE materials MODIFY COLUMN minimum_stock BIGINT NOT NULL DEFAULT 0, MODIFY COLUMN maximum_stock BIGINT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE materials_widen FROM @stmt;
EXECUTE materials_widen;
DEALLOCATE PREPARE materials_widen;

SET @products_table_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'products');
SET @stmt := IF(@products_table_exists > 0, 'UPDATE products SET minimum_stock = ROUND(minimum_stock, 0), maximum_stock = ROUND(maximum_stock, 0)', 'SELECT 1');
PREPARE products_round FROM @stmt;
EXECUTE products_round;
DEALLOCATE PREPARE products_round;
SET @stmt := IF(@products_table_exists > 0, 'ALTER TABLE products MODIFY COLUMN minimum_stock BIGINT NOT NULL DEFAULT 0, MODIFY COLUMN maximum_stock BIGINT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE products_widen FROM @stmt;
EXECUTE products_widen;
DEALLOCATE PREPARE products_widen;
