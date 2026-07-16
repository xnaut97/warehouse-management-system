package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.customer.CustomerRequest;
import com.github.xnaut97.wms.dto.customer.CustomerResponse;
import com.github.xnaut97.wms.dto.customer.UpdateCustomerRequest;
import com.github.xnaut97.wms.entity.common.Customer;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    @Audit(
            action = AuditAction.CREATE,
            entity = "Customer"
    )
    @Transactional
    public CustomerResponse create(CustomerRequest request) {

        if (request.getEmail() != null &&
                repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists.");
        }

        Customer customer = new Customer();

        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setEnabled(true);

        repository.save(customer);

        return map(customer);
    }

    @Transactional
    public Page<CustomerResponse> getAll(Pageable pageable) {

        return repository.findAll(pageable)
                .map(this::map);

    }

    @Transactional
    public CustomerResponse getById(Long id) {

        return map(findCustomerById(id));

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Customer"
    )
    @Transactional
    public CustomerResponse update(
            Long id,
            UpdateCustomerRequest request
    ) {

        Customer customer = findCustomerById(id);

        if (request.getEmail() != null
                && !request.getEmail().equals(customer.getEmail())
                && repository.existsByEmail(request.getEmail())) {

            throw new BusinessException(
                    "Email already exists."
            );

        }

        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());

        repository.save(customer);

        return map(customer);

    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "Customer"
    )
    @Transactional
    public void delete(Long id) {

        Customer customer = findCustomerById(id);
        if(customer == null) return;

        repository.delete(customer);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Customer"
    )
    @Transactional
    public void enable(Long id) {

        Customer customer = findCustomerById(id);

        customer.setEnabled(true);

        repository.save(customer);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Customer"
    )
    @Transactional
    public void disable(Long id) {

        Customer customer = findCustomerById(id);

        customer.setEnabled(false);

        repository.save(customer);

    }

    public Customer findCustomerById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Customer not found."
                        ));

    }

    private CustomerResponse map(Customer customer) {

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .enabled(customer.getEnabled())
                .build();

    }

}