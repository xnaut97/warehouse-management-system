package com.github.xnaut97.wms.service.warehouse;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.common.PageResponse;
import com.github.xnaut97.wms.dto.material.MaterialRequest;
import com.github.xnaut97.wms.dto.material.MaterialResponse;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.RawMaterialRepository;
import com.github.xnaut97.wms.service.SupplierService;
import com.github.xnaut97.wms.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RawMaterialService {

    private final RawMaterialRepository repository;

    private final SupplierService supplierService;

    public PageResponse<MaterialResponse> getAll(Pageable pageable) {

        Page<RawMaterial> page = repository.findAll(pageable);

        return PageUtils.from(page, this::map);

    }

    public MaterialResponse getById(Long id) {

        return map(findMaterialById(id));

    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "Material"
    )
    public MaterialResponse create(MaterialRequest request) {

        if (repository.existsByCode(request.getCode())) {
            throw new BusinessException("Material code already exists");
        }

        Supplier supplier =
                supplierService.findSupplierById(request.getSupplierId());

        RawMaterial material = new RawMaterial();

        material.setCode(request.getCode());
        material.setName(request.getName());
        material.setUnit(request.getUnit());
        material.setUnitPrice(request.getUnitPrice());
        material.setMinimumStock(request.getMinimumStock());
        material.setSupplier(supplier);
        material.setEnabled(true);

        repository.save(material);

        return map(material);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Material"
    )
    public MaterialResponse update(Long id, MaterialRequest request) {

        RawMaterial material = findMaterialById(id);

        if (!material.getCode().equals(request.getCode())
                && repository.existsByCode(request.getCode())) {
            throw new BusinessException("Material code already exists");
        }

        Supplier supplier =
                supplierService.findSupplierById(request.getSupplierId());

        material.setCode(request.getCode());
        material.setName(request.getName());
        material.setUnit(request.getUnit());
        material.setUnitPrice(request.getUnitPrice());
        material.setMinimumStock(request.getMinimumStock());
        material.setSupplier(supplier);

        repository.save(material);

        return map(material);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Material"
    )
    public MaterialResponse disable(Long id) {

        RawMaterial material = findMaterialById(id);

        material.setEnabled(false);

        repository.save(material);

        return map(material);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Material"
    )
    public MaterialResponse enable(Long id) {

        RawMaterial material = findMaterialById(id);

        material.setEnabled(true);

        repository.save(material);

        return map(material);

    }

    public PageResponse<MaterialResponse> search(
            String keyword,
            Pageable pageable
    ) {

        Page<RawMaterial> page =
                repository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                );

        return PageUtils.from(page, this::map);

    }

    private MaterialResponse map(RawMaterial material) {

        return MaterialResponse.builder()
                .id(material.getId())
                .code(material.getCode())
                .name(material.getName())
                .unit(material.getUnit())
                .unitPrice(material.getUnitPrice())
                .minimumStock(material.getMinimumStock())
                .supplierId(material.getSupplier().getId())
                .supplierName(material.getSupplier().getName())
                .enabled(material.getEnabled())
                .build();

    }

    public RawMaterial findMaterialById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("Material not found"));

    }
}