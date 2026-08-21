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

        if (repository.existsByCode(request.getCode())) {
            throw new BusinessException("Mã khách hàng đã tồn tại.");
        }

        if (hasText(request.getEmail()) &&
                repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã tồn tại.");
        }

        Customer customer = new Customer();

        customer.setCode(request.getCode());
        customer.setName(request.getName());
        customer.setAddress(request.getAddress());
        customer.setCustomerGroup(request.getCustomerGroup());
        customer.setReceiverName(request.getReceiverName());
        customer.setPhone(request.getPhone());
        customer.setEmail(normalizeEmail(request.getEmail()));
        customer.setNote(request.getNote());
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

        if (!request.getCode().equals(customer.getCode())
                && repository.existsByCode(request.getCode())) {

            throw new BusinessException(
                    "Mã khách hàng đã tồn tại."
            );

        }

        if (hasText(request.getEmail())
                && !request.getEmail().equals(customer.getEmail())
                && repository.existsByEmail(request.getEmail())) {

            throw new BusinessException(
                    "Email đã tồn tại."
            );

        }

        customer.setCode(request.getCode());
        customer.setName(request.getName());
        customer.setAddress(request.getAddress());
        customer.setCustomerGroup(request.getCustomerGroup());
        customer.setReceiverName(request.getReceiverName());
        customer.setPhone(request.getPhone());
        customer.setEmail(normalizeEmail(request.getEmail()));
        customer.setNote(request.getNote());

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
                                "Không tìm thấy khách hàng."
                        ));

    }

    private CustomerResponse map(Customer customer) {

        return CustomerResponse.builder()
                .id(customer.getId())
                .code(customer.getCode())
                .name(customer.getName())
                .address(customer.getAddress())
                .customerGroup(customer.getCustomerGroup())
                .receiverName(customer.getReceiverName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .note(customer.getNote())
                .enabled(customer.getEnabled())
                .build();

    }

    private boolean hasText(String value) {

        return value != null && !value.isBlank();

    }

    private String normalizeEmail(String email) {

        return hasText(email) ? email : null;

    }

}
