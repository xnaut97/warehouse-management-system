package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.common.PageResponse;
import com.github.xnaut97.wms.dto.supplier.SupplierRequest;
import com.github.xnaut97.wms.dto.supplier.SupplierResponse;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository repository;

    public PageResponse<SupplierResponse> getAll(Pageable pageable) {

        Page<Supplier> page = repository.findAll(pageable);

        return PageResponse.<SupplierResponse>builder()
                .content(
                        page.getContent()
                                .stream()
                                .map(this::map)
                                .toList()
                )
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

    }

    public SupplierResponse getById(Long id) {

        return map(findSupplierById(id));

    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "Supplier"
    )
    public SupplierResponse create(SupplierRequest request){

        if(repository.existsByCode(request.getCode())){
            throw new BusinessException("Supplier code already exists");
        }

        Supplier supplier = new Supplier();

        supplier.setCode(request.getCode());
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());

        repository.save(supplier);

        return map(supplier);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Warehouse"
    )
    public SupplierResponse update(Long id, SupplierRequest request) {

        Supplier supplier = findSupplierById(id);

        if (!supplier.getCode().equals(request.getCode())
                && repository.existsByCode(request.getCode())) {

            throw new BusinessException("Supplier code already exists");
        }

        supplier.setCode(request.getCode());
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());

        repository.save(supplier);

        return map(supplier);

    }


    @Audit(
            action = AuditAction.DELETE,
            entity = "Supplier"
    )
    public void delete(Long id) {
        Supplier supplier = findSupplierById(id);
        if(supplier == null) return;
        repository.delete(supplier);
    }

    public List<SupplierResponse> search(String keyword) {

        return repository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                        keyword,
                        keyword
                )
                .stream()
                .map(this::map)
                .toList();

    }

    private SupplierResponse map(Supplier supplier){

        return SupplierResponse.builder()
                .id(supplier.getId())
                .code(supplier.getCode())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .build();

    }

    public Supplier findSupplierById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("Supplier not found"));

    }

}