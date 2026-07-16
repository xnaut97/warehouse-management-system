package com.github.xnaut97.wms.repository;

import com.github.xnaut97.wms.entity.common.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

}