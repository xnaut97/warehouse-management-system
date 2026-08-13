package com.github.xnaut97.wms.service.warehouse;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.common.PageResponse;
import com.github.xnaut97.wms.dto.material.MaterialRequest;
import com.github.xnaut97.wms.dto.material.MaterialResponse;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.material.Supplier;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.service.SupplierService;
import com.github.xnaut97.wms.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository repository;

    private final SupplierService supplierService;

    public PageResponse<MaterialResponse> getAll(Pageable pageable) {

        Page<Material> page = repository.findAll(pageable);

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

        validateStockRange(
                request.getMinimumStock(),
                request.getMaximumStock()
        );

        if (repository.existsByCode(request.getCode())) {
            throw new BusinessException("Mã nguyên liệu đã tồn tại");
        }

        Supplier supplier =
                supplierService.findSupplierById(request.getSupplierId());

        Material material = new Material();

        material.setCode(request.getCode());
        material.setName(request.getName());
        material.setUnit(request.getUnit());
        material.setUnitPrice(request.getUnitPrice());
        material.setMinimumStock(request.getMinimumStock());
        material.setMaximumStock(request.getMaximumStock());
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

        Material material = findMaterialById(id);

        validateStockRange(
                request.getMinimumStock(),
                request.getMaximumStock()
        );

        if (!material.getCode().equals(request.getCode())
                && repository.existsByCode(request.getCode())) {
            throw new BusinessException("Mã nguyên liệu đã tồn tại");
        }

        Supplier supplier =
                supplierService.findSupplierById(request.getSupplierId());

        material.setCode(request.getCode());
        material.setName(request.getName());
        material.setUnit(request.getUnit());
        material.setUnitPrice(request.getUnitPrice());
        material.setMinimumStock(request.getMinimumStock());
        material.setMaximumStock(request.getMaximumStock());
        material.setSupplier(supplier);

        repository.save(material);

        return map(material);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Material"
    )
    public MaterialResponse disable(Long id) {

        Material material = findMaterialById(id);

        material.setEnabled(false);

        repository.save(material);

        return map(material);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Material"
    )
    public MaterialResponse enable(Long id) {

        Material material = findMaterialById(id);

        material.setEnabled(true);

        repository.save(material);

        return map(material);

    }

    public PageResponse<MaterialResponse> search(
            String keyword,
            Pageable pageable
    ) {

        Page<Material> page =
                repository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                );

        return PageUtils.from(page, this::map);

    }

    private MaterialResponse map(Material material) {

        return MaterialResponse.builder()
                .id(material.getId())
                .code(material.getCode())
                .name(material.getName())
                .unit(material.getUnit())
                .unitPrice(material.getUnitPrice())
                .minimumStock(material.getMinimumStock())
                .maximumStock(material.getMaximumStock())
                .supplierId(material.getSupplier().getId())
                .supplierName(material.getSupplier().getName())
                .enabled(material.getEnabled())
                .build();

    }

    public Material findMaterialById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("Không tìm thấy nguyên liệu"));

    }

    private void validateStockRange(
            java.math.BigDecimal minimumStock,
            java.math.BigDecimal maximumStock
    ) {

        if (maximumStock.compareTo(minimumStock) < 0) {
            throw new BusinessException("Tồn max phải lớn hơn hoặc bằng tồn min.");
        }

    }
}
