package com.github.xnaut97.wms.repository;

import com.github.xnaut97.wms.entity.common.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    boolean existsByCode(String code);

    Optional<Customer> findByCode(String code);

}
