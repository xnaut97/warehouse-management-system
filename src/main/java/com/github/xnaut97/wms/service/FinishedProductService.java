package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.product.FinishedProductRequest;
import com.github.xnaut97.wms.dto.product.FinishedProductResponse;
import com.github.xnaut97.wms.dto.product.UpdateFinishedProductRequest;
import com.github.xnaut97.wms.entity.FinishedProduct;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.FinishedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinishedProductService {

    private final FinishedProductRepository repository;

    @Audit(
            action = AuditAction.CREATE,
            entity = "Finished Product"
    )
    public FinishedProductResponse create(
            FinishedProductRequest request
    ) {

        if (repository.existsByCode(request.getCode())) {
            throw new BusinessException(
                    "Finished product code already exists."
            );
        }

        FinishedProduct product = new FinishedProduct();

        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setSpecification(request.getSpecification());
        product.setUnit(request.getUnit());
        product.setSellingPrice(request.getSellingPrice());
        product.setEnabled(true);

        repository.save(product);

        return map(product);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Finished Product"
    )
    public FinishedProductResponse update(
            Long id,
            UpdateFinishedProductRequest request
    ) {

        FinishedProduct product =
                findFinishedProductById(id);

        product.setName(request.getName());
        product.setSpecification(request.getSpecification());
        product.setUnit(request.getUnit());
        product.setSellingPrice(request.getSellingPrice());
        product.setEnabled(request.getEnabled());

        repository.save(product);

        return map(product);

    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "Finished Product"
    )
    public void delete(Long id) {

        FinishedProduct product =
                findFinishedProductById(id);

        product.setEnabled(false);

        repository.save(product);

    }

    public Page<FinishedProductResponse> search(

            String keyword,

            int page,

            int size

    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        return repository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                )
                .map(this::map);

    }

    public FinishedProductResponse getById(Long id) {

        return map(
                findFinishedProductById(id)
        );

    }

    public Page<FinishedProductResponse> getAll(

            String keyword,

            int page,

            int size

    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        return repository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                )
                .map(this::map);

    }

    public FinishedProduct findFinishedProductById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Finished product not found."
                        ));

    }

    private FinishedProductResponse map(
            FinishedProduct product
    ) {

        return FinishedProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .specification(product.getSpecification())
                .unit(product.getUnit())
                .sellingPrice(product.getSellingPrice())
                .enabled(product.getEnabled())
                .build();

    }

}