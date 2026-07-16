package com.github.xnaut97.wms.specification;

import com.github.xnaut97.wms.entity.stock.Stocktaking;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class StocktakingSpecification {

    public static Specification<Stocktaking> search(

            String keyword,

            Long warehouseId,

            StocktakingStatus status,

            LocalDate fromDate,

            LocalDate toDate

    ) {

        return (root, query, cb) -> {

            var predicate = cb.conjunction();

            if (keyword != null && !keyword.isBlank()) {

                predicate = cb.and(

                        predicate,

                        cb.like(

                                cb.lower(root.get("stocktakingNo")),

                                "%" + keyword.toLowerCase() + "%"

                        )

                );

            }

            if (warehouseId != null) {

                predicate = cb.and(

                        predicate,

                        cb.equal(

                                root.get("warehouse").get("id"),

                                warehouseId

                        )

                );

            }

            if (status != null) {

                predicate = cb.and(

                        predicate,

                        cb.equal(

                                root.get("status"),

                                status

                        )

                );

            }

            if (fromDate != null) {

                predicate = cb.and(

                        predicate,

                        cb.greaterThanOrEqualTo(

                                root.get("stocktakingDate"),

                                fromDate

                        )

                );

            }

            if (toDate != null) {

                predicate = cb.and(

                        predicate,

                        cb.lessThanOrEqualTo(

                                root.get("stocktakingDate"),

                                toDate

                        )

                );

            }

            return predicate;

        };

    }

}