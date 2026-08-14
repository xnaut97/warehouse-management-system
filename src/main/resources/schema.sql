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
