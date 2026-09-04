package com.github.xnaut97.wms.service.product;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.product.ProductRequest;
import com.github.xnaut97.wms.dto.product.ProductResponse;
import com.github.xnaut97.wms.dto.product.UpdateProductRequest;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.product.ProductIssueItemRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "Keo C1",
            "Keo 2"
    );

    private static final int PRICE_SCALE = 2;

    private final ProductRepository repository;

    private final ProductIssueItemRepository issueItemRepository;

    @Audit(
            action = AuditAction.CREATE,
            entity = "Finished Product"
    )
    public ProductResponse create(
            ProductRequest request
    ) {

        if (repository.existsByCode(request.getCode())) {
            throw new BusinessException(
                    "Mã thành phẩm đã tồn tại."
            );
        }

        validateStockRange(
                request.getMinimumStock(),
                request.getMaximumStock()
        );

        Product product = new Product();

        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setSpecification(request.getSpecification());
        product.setUnit(request.getUnit());
        product.setAveragePrice(BigDecimal.ZERO);
        product.setCategory(validateCategory(request.getCategory()));
        product.setMinimumStock(request.getMinimumStock());
        product.setMaximumStock(request.getMaximumStock());
        product.setEnabled(true);

        repository.save(product);

        return map(product);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Finished Product"
    )
    public ProductResponse update(
            Long id,
            UpdateProductRequest request
    ) {

        Product product =
                findProductById(id);

        validateStockRange(
                request.getMinimumStock(),
                request.getMaximumStock()
        );

        product.setName(request.getName());
        product.setSpecification(request.getSpecification());
        product.setUnit(request.getUnit());
        product.setCategory(validateCategory(request.getCategory()));
        product.setMinimumStock(request.getMinimumStock());
        product.setMaximumStock(request.getMaximumStock());
        product.setEnabled(request.getEnabled());

        repository.save(product);

        return map(product);

    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "Finished Product"
    )
    public void delete(Long id) {

        Product product = findProductById(id);

        repository.delete(product);

    }

    public Page<ProductResponse> search(

            String keyword,

            int page,

            int size

    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        return repository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword,
                        pageable
                )
                .map(this::map);

    }

    public ProductResponse getById(Long id) {

        return map(
                findProductById(id)
        );

    }

    public Page<ProductResponse> getAll(

            String keyword,

            int page,

            int size

    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        return repository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword,
                        pageable
                )
                .map(this::map);

    }

    public Product findProductById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy thành phẩm."
                        ));

    }

    public Product findActiveProductById(Long id) {
        Product product = findProductById(id);

        if (!Boolean.TRUE.equals(product.getEnabled())) {
            throw new BusinessException(
                    "Sản phẩm đã bị khóa."
            );
        }

        return product;
    }

    /**
     * Giá trung bình của sản phẩm được tính từ các phiếu xuất đã xác nhận,
     * theo bình quân gia quyền trên số lượng xuất.
     */
    public void recalculateAveragePrice(Long productId) {

        Product product = findProductById(productId);

        BigDecimal averagePrice =
                issueItemRepository.calculateAveragePrice(
                        productId,
                        IssueStatus.CONFIRMED
                );

        product.setAveragePrice(
                averagePrice == null
                        ? BigDecimal.ZERO
                        : averagePrice.setScale(
                                PRICE_SCALE,
                                RoundingMode.HALF_UP
                        )
        );

        repository.save(product);

    }

    private ProductResponse map(
            Product product
    ) {

        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .specification(product.getSpecification())
                .unit(product.getUnit())
                .averagePrice(product.getAveragePrice())
                .category(product.getCategory())
                .minimumStock(product.getMinimumStock())
                .maximumStock(product.getMaximumStock())
                .enabled(product.getEnabled())
                .build();

    }

    private String validateCategory(String category) {

        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new BusinessException("Phân loại sản phẩm không hợp lệ.");
        }

        return category;

    }

    private void validateStockRange(
            BigDecimal minimumStock,
            BigDecimal maximumStock
    ) {

        if (maximumStock.compareTo(minimumStock) < 0) {
            throw new BusinessException("Tồn max phải lớn hơn hoặc bằng tồn min.");
        }

    }

}
