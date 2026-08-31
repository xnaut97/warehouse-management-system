package com.github.xnaut97.wms.factory;

import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.entity.goods.GoodsIssueItem;
import com.github.xnaut97.wms.entity.goods.GoodsReceipt;
import com.github.xnaut97.wms.entity.goods.GoodsReceiptItem;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.entity.product.ProductIssue;
import com.github.xnaut97.wms.entity.product.ProductIssueItem;
import com.github.xnaut97.wms.entity.product.ProductReceipt;
import com.github.xnaut97.wms.entity.product.ProductReceiptItem;
import com.github.xnaut97.wms.entity.stock.Stocktaking;
import com.github.xnaut97.wms.entity.stock.StocktakingItem;
import com.github.xnaut97.wms.entity.stock.StocktakingItemBatch;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.user.Role;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.CustomerGroup;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import com.github.xnaut97.wms.enums.RoleType;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StocktakingItemStatus;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import com.github.xnaut97.wms.enums.StocktakingType;
import com.github.xnaut97.wms.enums.SupplierGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SampleDataFactory {

    private final PasswordEncoder passwordEncoder;

    public Role role(RoleType roleType) {

        Role role = new Role();

        role.setRole(roleType);

        role.setDescription(roleType.name());

        return role;
    }

    public User user(
            String username,
            String fullName,
            String email,
            Role role
    ) {

        User user = new User();

        user.setUsername(username);

        user.setPassword(passwordEncoder.encode(username + "123"));

        user.setFullName(fullName);

        user.setEmail(email);

        user.setEnabled(true);

        user.setRole(role);

        return user;
    }

    public Warehouse warehouse(
            String code,
            String name,
            String address,
            String description,
            User manager
    ) {

        Warehouse warehouse = new Warehouse();

        warehouse.setCode(code);

        warehouse.setManager(manager);

        warehouse.setName(name);

        warehouse.setAddress(address);

        warehouse.setDescription(description);

        warehouse.setEnabled(true);

        return warehouse;
    }

    public Supplier supplier(
            String code,
            String name,
            SupplierGroup supplierGroup
    ) {

        Supplier supplier = new Supplier();

        supplier.setCode(code);

        supplier.setName(name);

        supplier.setSupplierGroup(supplierGroup);

        supplier.setContactPerson("Nguyen Van A");

        supplier.setPhone("0900000000");

        supplier.setEmail(code.toLowerCase() + "@mail.com");

        supplier.setAddress("Ho Chi Minh City");

        return supplier;
    }

    public Customer customer(
            String code,
            String name,
            CustomerGroup customerGroup
    ) {

        Customer customer = new Customer();

        customer.setCode(code);

        customer.setName(name);

        customer.setCustomerGroup(customerGroup);

        customer.setReceiverName("Nguyen Van B");

        customer.setPhone("0911111111");

        customer.setEmail(code.toLowerCase() + "@mail.com");

        customer.setAddress("Ho Chi Minh City");

        customer.setEnabled(true);

        return customer;
    }

    public Material material(
            String code,
            String name,
            String unit,
            BigDecimal price,
            BigDecimal minimumStock,
            BigDecimal maximumStock,
            Supplier supplier
    ) {

        Material material = new Material();

        material.setCode(code);

        material.setName(name);

        material.setUnit(unit);

        material.setUnitPrice(price);

        material.setMinimumStock(minimumStock);

        material.setMaximumStock(maximumStock);

        material.setSupplier(supplier);

        material.setEnabled(true);

        return material;
    }

    public Product product(

            String code,

            String name,

            String specification,

            String unit,

            String category,

            BigDecimal minimumStock,

            BigDecimal maximumStock

    ){

        Product product = new Product();

        product.setCode(code);

        product.setName(name);

        product.setSpecification(specification);

        product.setUnit(unit);

        product.setAveragePrice(BigDecimal.ZERO);

        product.setMinimumStock(minimumStock);

        product.setMaximumStock(maximumStock);

        product.setCategory(category);

        product.setEnabled(true);

        return product;

    }

    public GoodsReceipt goodsReceipt(
            String receiptNo,
            Supplier supplier,
            Warehouse warehouse,
            LocalDate receiptDate,
            ReceiptStatus status,
            User createdBy
    ) {

        GoodsReceipt receipt = new GoodsReceipt();

        receipt.setReceiptNo(receiptNo);

        receipt.setSupplier(supplier);

        receipt.setWarehouse(warehouse);

        receipt.setReceiptDate(receiptDate);

        receipt.setStatus(status);

        receipt.setTotalAmount(BigDecimal.ZERO);

        receipt.setCreatedBy(createdBy);

        return receipt;
    }

    public GoodsReceiptItem goodsReceiptItem(
            GoodsReceipt receipt,
            Material material,
            BigDecimal quantity,
            BigDecimal unitPrice
    ) {

        GoodsReceiptItem item = new GoodsReceiptItem();

        item.setReceipt(receipt);

        item.setMaterial(material);

        item.setQuantity(quantity);

        item.setUnitPrice(unitPrice);

        item.setAmount(quantity.multiply(unitPrice));

        return item;
    }

    public GoodsIssue goodsIssue(
            String issueNo,
            Warehouse warehouse,
            Customer customer,
            LocalDate issueDate,
            IssueStatus status,
            User createdBy
    ) {

        GoodsIssue issue = new GoodsIssue();

        issue.setIssueNo(issueNo);

        issue.setWarehouse(warehouse);

        issue.setCustomer(customer);

        issue.setIssueDate(issueDate);

        issue.setStatus(status);

        issue.setTotalAmount(BigDecimal.ZERO);

        issue.setCreatedBy(createdBy);

        return issue;
    }

    public GoodsIssueItem goodsIssueItem(
            GoodsIssue issue,
            Material material,
            BigDecimal quantity,
            BigDecimal unitPrice
    ) {

        GoodsIssueItem item = new GoodsIssueItem();

        item.setIssue(issue);

        item.setMaterial(material);

        item.setQuantity(quantity);

        item.setUnitPrice(unitPrice);

        item.setAmount(quantity.multiply(unitPrice));

        return item;
    }

    public ProductReceipt productReceipt(
            String receiptNo,
            Warehouse warehouse,
            LocalDate receiptDate,
            ReceiptStatus status,
            User createdBy
    ) {

        ProductReceipt receipt = new ProductReceipt();

        receipt.setReceiptNo(receiptNo);

        receipt.setWarehouse(warehouse);

        receipt.setReceiptDate(receiptDate);

        receipt.setStatus(status);

        receipt.setTotalAmount(BigDecimal.ZERO);

        receipt.setCreatedBy(createdBy);

        return receipt;
    }

    public ProductReceiptItem productReceiptItem(
            ProductReceipt receipt,
            Product product,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String lotNumber,
            LocalDate expirationDate
    ) {

        ProductReceiptItem item = new ProductReceiptItem();

        item.setReceipt(receipt);

        item.setProduct(product);

        item.setQuantity(quantity);

        item.setUnitPrice(unitPrice);

        item.setAmount(quantity.multiply(unitPrice));

        item.setLotNumber(lotNumber);

        item.setExpirationDate(expirationDate);

        return item;
    }

    public ProductIssue productIssue(
            String issueNo,
            Warehouse warehouse,
            Customer customer,
            LocalDate issueDate,
            IssueStatus status,
            User createdBy
    ) {

        ProductIssue issue = new ProductIssue();

        issue.setIssueNo(issueNo);

        issue.setWarehouse(warehouse);

        issue.setCustomer(customer);

        issue.setIssueDate(issueDate);

        issue.setStatus(status);

        issue.setTotalAmount(BigDecimal.ZERO);

        issue.setCreatedBy(createdBy);

        return issue;
    }

    public ProductIssueItem productIssueItem(
            ProductIssue issue,
            Product product,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String lotNumber,
            LocalDate expirationDate
    ) {

        ProductIssueItem item = new ProductIssueItem();

        item.setIssue(issue);

        item.setProduct(product);

        item.setQuantity(quantity);

        item.setUnitPrice(unitPrice);

        item.setAmount(quantity.multiply(unitPrice));

        item.setLotNumber(lotNumber);

        item.setExpirationDate(expirationDate);

        return item;
    }

    public MaterialInventory materialInventory(
            Warehouse warehouse,
            Material material
    ) {

        MaterialInventory inventory = new MaterialInventory();

        inventory.setWarehouse(warehouse);

        inventory.setMaterial(material);

        inventory.setQuantity(BigDecimal.ZERO);

        return inventory;
    }

    public ProductInventory productInventory(
            Warehouse warehouse,
            Product product,
            String lotNumber,
            LocalDate expirationDate
    ) {

        ProductInventory inventory = new ProductInventory();

        inventory.setWarehouse(warehouse);

        inventory.setProduct(product);

        inventory.setLotNumber(lotNumber);

        inventory.setExpirationDate(expirationDate);

        inventory.setQuantity(BigDecimal.ZERO);

        return inventory;
    }

    public InventoryTransaction inventoryTransaction(
            Warehouse warehouse,
            Material material,
            InventoryTransactionType type,
            BigDecimal quantity,
            String referenceNo,
            User createdBy
    ) {

        InventoryTransaction transaction = new InventoryTransaction();

        transaction.setWarehouse(warehouse);

        transaction.setMaterial(material);

        transaction.setType(type);

        transaction.setQuantity(quantity);

        transaction.setReferenceNo(referenceNo);

        transaction.setCreatedBy(createdBy);

        return transaction;
    }

    public Stocktaking stocktaking(
            String stocktakingNo,
            Warehouse warehouse,
            LocalDate stocktakingDate,
            StocktakingType type,
            StocktakingStatus status,
            String note,
            User stocktaker,
            User createdBy
    ) {

        Stocktaking stocktaking = new Stocktaking();

        stocktaking.setStocktakingNo(stocktakingNo);

        stocktaking.setWarehouse(warehouse);

        stocktaking.setStocktakingDate(stocktakingDate);

        stocktaking.setType(type);

        stocktaking.setStatus(status);

        stocktaking.setNote(note);

        stocktaking.setStocktaker(stocktaker);

        stocktaking.setCreatedBy(createdBy);

        return stocktaking;
    }

    public StocktakingItem stocktakingItem(
            Stocktaking stocktaking,
            StockGroup itemGroup,
            Material material,
            Product product,
            BigDecimal systemQuantity,
            BigDecimal physicalQuantity,
            String reason
    ) {

        StocktakingItem item = new StocktakingItem();

        item.setStocktaking(stocktaking);

        item.setItemGroup(itemGroup);

        item.setMaterial(material);

        item.setProduct(product);

        item.setSystemQuantity(systemQuantity);

        item.setReason(reason);

        item.setPhysicalQuantity(physicalQuantity);

        if (physicalQuantity == null) {

            item.setVarianceQuantity(BigDecimal.ZERO);

            item.setItemStatus(null);

            return item;
        }

        BigDecimal variance =
                physicalQuantity.subtract(systemQuantity);

        item.setVarianceQuantity(variance);

        item.setItemStatus(
                variance.compareTo(BigDecimal.ZERO) == 0
                        ? StocktakingItemStatus.MATCHED
                        : StocktakingItemStatus.DISCREPANCY
        );

        return item;
    }

    public StocktakingItemBatch stocktakingItemBatch(
            StocktakingItem item,
            ProductInventory inventory,
            BigDecimal systemQuantity,
            BigDecimal physicalQuantity,
            String reason
    ) {

        StocktakingItemBatch batch = new StocktakingItemBatch();

        batch.setItem(item);

        batch.setProductInventory(inventory);

        batch.setLotNumber(inventory.getLotNumber());

        batch.setExpirationDate(inventory.getExpirationDate());

        batch.setSystemQuantity(systemQuantity);

        batch.setPhysicalQuantity(physicalQuantity);

        batch.setReason(reason);

        batch.setVarianceQuantity(
                physicalQuantity == null
                        ? BigDecimal.ZERO
                        : physicalQuantity.subtract(systemQuantity)
        );

        return batch;
    }

}
