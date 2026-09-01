package com.github.xnaut97.wms.factory;

import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.user.Role;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.CustomerGroup;
import com.github.xnaut97.wms.enums.RoleType;
import com.github.xnaut97.wms.enums.SupplierGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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

}
