package com.github.xnaut97.wms.factory;

import com.github.xnaut97.wms.entity.FinishedProduct;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.entity.user.Role;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.RoleType;
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

    public Warehouse warehouse(String code, String name) {

        Warehouse warehouse = new Warehouse();

        warehouse.setCode(code);

        warehouse.setName(name);

        warehouse.setAddress(name + " Address");

        warehouse.setEnabled(true);

        return warehouse;
    }

    public Supplier supplier(
            String code,
            String name
    ) {

        Supplier supplier = new Supplier();

        supplier.setCode(code);

        supplier.setName(name);

        supplier.setContactPerson("Nguyen Van A");

        supplier.setPhone("0900000000");

        supplier.setEmail(code.toLowerCase() + "@mail.com");

        supplier.setAddress("Ho Chi Minh City");

        return supplier;
    }

    public Customer customer(
            String code,
            String name
    ) {

        Customer customer = new Customer();

        customer.setName(name);

        customer.setPhone("0911111111");

        customer.setEmail(code.toLowerCase() + "@mail.com");

        customer.setAddress("Ho Chi Minh City");

        customer.setEnabled(true);

        return customer;
    }

    public RawMaterial material(
            String code,
            String name,
            String unit,
            BigDecimal price,
            Supplier supplier
    ) {

        RawMaterial material = new RawMaterial();

        material.setCode(code);

        material.setName(name);

        material.setUnit(unit);

        material.setUnitPrice(price);

        material.setMinimumStock(BigDecimal.valueOf(50));

        material.setSupplier(supplier);

        material.setEnabled(true);

        return material;
    }

    public FinishedProduct finishedProduct(

            String code,

            String name,

            String specification,

            String unit,

            BigDecimal price

    ){

        FinishedProduct product = new FinishedProduct();

        product.setCode(code);

        product.setName(name);

        product.setSpecification(specification);

        product.setUnit(unit);

        product.setSellingPrice(price);

        product.setEnabled(true);

        return product;

    }

}