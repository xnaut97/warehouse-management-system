package com.github.xnaut97.wms.service.bom;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.bom.BOMItemRequest;
import com.github.xnaut97.wms.dto.bom.BOMItemResponse;
import com.github.xnaut97.wms.dto.bom.BOMRequest;
import com.github.xnaut97.wms.dto.bom.BOMResponse;
import com.github.xnaut97.wms.entity.bom.BOM;
import com.github.xnaut97.wms.entity.bom.BOMItem;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.bom.BOMRepository;
import com.github.xnaut97.wms.service.product.ProductService;
import com.github.xnaut97.wms.service.warehouse.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BOMService {

    private final BOMRepository repository;

    private final ProductService productService;

    private final MaterialService materialService;


    @Transactional(readOnly = true)
    public List<BOMResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();

    }


    @Transactional(readOnly = true)
    public BOMResponse getById(Long id) {

        return map(findBOMById(id));

    }


    @Transactional
    @Audit(
            action = AuditAction.CREATE,
            entity = "BOM"
    )
    public BOMResponse create(BOMRequest request) {

        validateCodeNotExists(request.getCode());

        Product product =
                productService.findActiveProductById(
                        request.getProductId()
                );

        validateItems(request.getItems());

        BOM bom = new BOM();

        bom.setCode(request.getCode());
        bom.setProduct(product);
        bom.setEnabled(true);
        bom.setItems(new ArrayList<>());

        for (BOMItemRequest itemRequest : request.getItems()) {

            Material material =
                    materialService.findMaterialById(
                            itemRequest.getMaterialId()
                    );

            validateMaterialEnabled(material);

            BOMItem item = new BOMItem();

            item.setBom(bom);
            item.setMaterial(material);
            item.setConsumptionQuantity(
                    itemRequest.getConsumptionQuantity()
            );
            item.setMixingRatio(
                    itemRequest.getMixingRatio()
            );
            item.setMaxWasteRatio(
                    itemRequest.getMaxWasteRatio()
            );

            bom.getItems().add(item);
        }

        repository.save(bom);

        return map(bom);

    }


    @Transactional
    @Audit(
            action = AuditAction.UPDATE,
            entity = "BOM"
    )
    public BOMResponse update(
            Long id,
            BOMRequest request
    ) {

        BOM bom = findBOMById(id);

        validateCodeForUpdate(
                bom,
                request.getCode()
        );

        Product product =
                productService.findActiveProductById(
                        request.getProductId()
                );

        validateItems(request.getItems());

        bom.setCode(request.getCode());
        bom.setProduct(product);

        bom.getItems().clear();

        repository.saveAndFlush(bom);

        for (BOMItemRequest itemRequest : request.getItems()) {

            Material material =
                    materialService.findMaterialById(
                            itemRequest.getMaterialId()
                    );

            validateMaterialEnabled(material);

            BOMItem item = new BOMItem();

            item.setBom(bom);
            item.setMaterial(material);
            item.setConsumptionQuantity(
                    itemRequest.getConsumptionQuantity()
            );
            item.setMixingRatio(
                    itemRequest.getMixingRatio()
            );
            item.setMaxWasteRatio(
                    itemRequest.getMaxWasteRatio()
            );

            bom.getItems().add(item);
        }

        repository.save(bom);

        return map(bom);

    }


    @Transactional
    @Audit(
            action = AuditAction.UPDATE,
            entity = "BOM"
    )
    public BOMResponse disable(Long id) {

        BOM bom = findBOMById(id);

        bom.setEnabled(false);

        repository.save(bom);

        return map(bom);

    }


    @Transactional
    @Audit(
            action = AuditAction.UPDATE,
            entity = "BOM"
    )
    public BOMResponse enable(Long id) {

        BOM bom = findBOMById(id);

        bom.setEnabled(true);

        repository.save(bom);

        return map(bom);

    }


    @Transactional(readOnly = true)
    public List<BOMResponse> search(String keyword) {

        return repository
                .findByCodeContainingIgnoreCase(keyword)
                .stream()
                .map(this::map)
                .toList();

    }


    public BOM findBOMById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy BOM."
                        )
                );

    }


    private void validateCodeNotExists(String code) {

        if (repository.existsByCode(code)) {

            throw new BusinessException(
                    "Mã BOM đã tồn tại."
            );

        }

    }


    private void validateCodeForUpdate(
            BOM bom,
            String code
    ) {

        if (!bom.getCode().equals(code)
                && repository.existsByCode(code)) {

            throw new BusinessException(
                    "Mã BOM đã tồn tại."
            );

        }

    }


    private void validateMaterialEnabled(
            Material material
    ) {

        if (!Boolean.TRUE.equals(material.getEnabled())) {

            throw new BusinessException(
                    "Không thể sử dụng nguyên vật liệu đã ngừng hoạt động."
            );

        }

    }


    private void validateItems(
            List<BOMItemRequest> items
    ) {

        Set<Long> materialIds = new HashSet<>();

        for (BOMItemRequest item : items) {

            if (!materialIds.add(item.getMaterialId())) {

                throw new BusinessException(
                        "Một nguyên vật liệu không thể xuất hiện nhiều lần trong cùng một BOM."
                );

            }

            validateRatio(
                    item.getMixingRatio(),
                    item.getMaxWasteRatio()
            );

        }

    }


    private void validateRatio(
            BigDecimal mixingRatio,
            BigDecimal maxWasteRatio
    ) {

        if (mixingRatio.compareTo(BigDecimal.ZERO) < 0
                || mixingRatio.compareTo(BigDecimal.valueOf(100)) > 0) {

            throw new BusinessException(
                    "Tỷ lệ phối trộn phải nằm trong khoảng từ 0 đến 100."
            );

        }

        if (maxWasteRatio.compareTo(BigDecimal.ZERO) < 0
                || maxWasteRatio.compareTo(BigDecimal.valueOf(100)) > 0) {

            throw new BusinessException(
                    "Tỷ lệ hao hụt tối đa phải nằm trong khoảng từ 0 đến 100."
            );

        }

    }


    private BOMResponse map(BOM bom) {

        List<BOMItemResponse> items =
                bom.getItems()
                        .stream()
                        .map(this::mapItem)
                        .toList();

        return BOMResponse.builder()
                .id(bom.getId())
                .code(bom.getCode())
                .productId(bom.getProduct().getId())
                .productCode(bom.getProduct().getCode())
                .productName(bom.getProduct().getName())
                .enabled(bom.getEnabled())
                .items(items)
                .build();

    }


    private BOMItemResponse mapItem(
            BOMItem item
    ) {

        Material material =
                item.getMaterial();

        return BOMItemResponse.builder()
                .id(item.getId())
                .materialId(material.getId())
                .materialCode(material.getCode())
                .materialName(material.getName())
                .consumptionQuantity(
                        item.getConsumptionQuantity()
                )
                .unit(material.getUnit())
                .mixingRatio(
                        item.getMixingRatio()
                )
                .maxWasteRatio(
                        item.getMaxWasteRatio()
                )
                .build();

    }

}